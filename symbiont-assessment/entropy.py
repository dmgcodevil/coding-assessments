import hashlib

import psutil
import sys

BLOCK_SIZE = 8  # in bytes
NUM_OF_BLOCK = 4

BLOCK_0 = 0  # Free virtual memory
BLOCK_1 = 1  # CPU interrupts
BLOCK_2 = 2  # Net IO: packets_sent + packets_recv
BLOCK_3 = 3  # Disk IO: read_count + write_count

CHUNK_SIZE = 20  # in bytes, sha1


def get_bytes(n: int):
    return n.to_bytes(BLOCK_SIZE, 'big')


class Entropy:
    pool = bytearray(BLOCK_SIZE * NUM_OF_BLOCK)

    # produces an array of random bytes of specified size
    def get_bytes(self, size: int = 20):
        out = bytearray(bytes(size))

        remaining = size
        chunks = int(size / CHUNK_SIZE)
        for i in range(chunks):
            buf = self.next_bytes()
            start = i * len(buf)
            end = start + len(buf)

            out[start:end] = buf
            remaining = remaining - len(buf)

        if remaining > 0:
            buf = self.next_bytes()
            out[chunks * len(buf):] = buf[0:remaining]
        return bytes(out)

    # produces an array of random bytes of size == CHUNK_SIZE
    def next_bytes(self):
        self.add_sources()
        sha1 = hashlib.sha1()
        sha1.update(bytes(self.pool))
        return sha1.digest()

    def add_source(self, s: bytes, block: int):
        start = block * BLOCK_SIZE
        end = start + BLOCK_SIZE
        self.pool[start:end] = s

    def add_free_memory(self):
        free = psutil.virtual_memory().free
        self.add_source(get_bytes(free), BLOCK_0)

    def add_cpu_usage(self):
        cpu_interrupts = psutil.cpu_stats().interrupts
        self.add_source(get_bytes(cpu_interrupts), BLOCK_1)

    def add_net_io(self):
        counters = psutil.net_io_counters()
        self.add_source(get_bytes(counters.packets_recv + counters.packets_sent), BLOCK_2)

    def add_disk_io(self):
        counters = psutil.disk_io_counters()
        self.add_source(get_bytes(counters.read_count + counters.write_count), BLOCK_3)

    def add_sources(self):
        self.add_free_memory()
        self.add_cpu_usage()
        self.add_net_io()
        self.add_disk_io()


size_arg = CHUNK_SIZE

if len(sys.argv) - 1 > 0:
    size_arg = int(sys.argv[1])

e = Entropy()
e.add_sources()

res = e.get_bytes(size_arg)

print(res)
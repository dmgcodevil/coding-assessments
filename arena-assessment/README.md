Before jumping into writing code right away I'm going to do some research, go through all possible solutions I can think of, compare time and space complexity of different solutions. (time limit 15 mins)

Solution 1:
Approach:
Read the file line by line, split words and put them into the hash table where the key is a word and value is a  set of line numbers that contain the given word.
Then iterate through the table and remove entries where the number of lines less than 50.
Then, create all possible pairs across all available candidates, intersect sets of line numbers of any two words,
if the size of intersection is >=  50  then add that pair to the result.
If we want we can sort the result by the number of common lines in descending order (optional).

Time complexity analyzes.

Splitting a line into words is O(N) where N  is a line length.
Parsing the whole file will take M*N where M  is the total number of lines.
For any given word we put it into the map and add the line number to the set of line numbers associated with that word. Both operations take constant time, i.e. O(1)

Remove all words from the map that appear in less than 50  lines. This operation takes O(N)  where N  is the number of unique words in the map.

Build pairs from the remaining words 'using' intersect operation on sets.
Let's define a pair as (w1, w2) then if intersect (map(w1), map(w2)) >=  50  then add that pair to the answer.  Time complexity is O(N^2) where  N is the number of unique words that appear in more than 50 lines.

Intersect  operation can be optimized for this particular task: we can interrupt the loop if the number of matched  lines equals 50  or if 
[[[
let min_len = min(len(w1), len(w2))
let match_count

if (min_len - i < 50  - match_count) then return
]]]

Taking into account these optimizations we can conclude that time complexity of 'intersect' operation in the worst  case  is  O(N)  where N is min(len(w1), len(w2))

Total time is O(N^2 * M) where M is the size of the shortest  set of line numbers.

Space complexity:  

Map[String, Set[Integer]] 
map size is O(N)  where N is the number of unique words
Set[Integer] is O(M) where M is the total number of lines
Total space is (N+N*M)

Assumptions:
Let's try to define a worst-case scenario for this solution.

* All lines contain the same words.
In this case, the size of the map would be 50, i.e.  2,500 possible pairs.
N^2  time complexity for N = 50 isn't bad but it doesn't scale well.



Pros:
The solution is easy to implement.
Cons:
O(N^2) Doesn't scale well

Other thoughts:
If any two words (w1,w2) appear in all lines then we can conclude that (w1,w2) is a valid pair
if we can build an answer based on this observation.



Maven is used as a build tool for this project

To build the project: mvn install
To run test: mvn test

How to use:

After `mvn install` go to `./target`
from terminal: `java -jar assessment-1.0-SNAPSHOT.jar ./Artist_lists_small.txt ./out.csv`
assuming that `Artist_lists_small.txt` locates in the `./target` directory
Project works supports both relative and absolute paths

Third argument is `leastNumOfLines` is optional and set to 50 by default
How to run:

It order to start a web sever you will need  SBT: https://www.scala-sbt.org/download.html

Once you downloaded and installed SBT run the following command from the project folder:

`sbt run`

it will start web server  on 8082 port

then in your favorite browser :  

http://localhost:8082/units/si?units=(degree/minute)

you should get the following response:

{
unit_name: "(rad/s)",
multiplication_factor: "0.00029088820867"
}


Using docker:

1. go to: ./citrine-assessment-package/docker
2. docker build -t citrine-assessment:0.1 ./
3. docker run --rm -p8080:8080 citrine-assessment:0.1
4. in browser: http://localhost:8080/units/si?units=(degree/minute)

TODO:

I didn't  have  a time to add validation  for the query parameter:

1. Check supported words and  characters:  Unit names , symbols(first two columns in the table), '/', '*', ''(', ')' . We  can use regexp 
2.  Check parentheses  using Stack: if current char is '('  push on stack, if ')' remove, check if  that stack is empty .  if yes then the string has well formed parentheses otherwise  - no.




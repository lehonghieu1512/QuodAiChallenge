# QuodAiChallenge

## Setup

Intellij

cannot use cmd line as it relates to a lot of dependencies

The order of input args:
\<start-time> \<end-time> \<input-json-file> \<output-csv-file>
  
For example: __2015-01-01T15:00:01Z 2017-01-01T15:00:01Z Document\abc.json Document\output.csv__

## What frameworks did I use?

Nothing special actually. Maybe the one that is the most special to me is simple-json. I used it to facilitate the way I process the data parsed from the json file

Since I am not really familiar with Java and have limited time. I decided to make it as simple as possible. Maybe, it is the most practical way I could come up with instead of messing around with fancy libraries and end up with frustration.

## Further improvements

Of course in this version there must be still a lot of flaws lurking inside it. I would improve the followings:

  Performance:It used different functions retrieving different kinds of data according to the requirements, which resulted in a lot of loops over the data set. I would rearrange the functions and reduce as many loops as I can.
  
  Command line: For now, I still fail to use command line to run java project with a lot of dependencies due to my lack of experience in java.
  
  Automatic download: This version can just receive a json file as its input. I would use big-query java api to retrieve data automatically.
  
  

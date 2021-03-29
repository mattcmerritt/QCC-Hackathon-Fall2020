# QCC-Hackathon-Fall2020 - Digital Privacy: Password Manager

A simple console program in Scala developed by Matthew Merritt and Michael Merritt for the Quinnipiac Computing Club Hackathon during the Fall 2020 semester.

The files used during the presentation are included in the ``Presentation`` directory.

To run the program, make sure you have Scala installed, open a terminal, and run

```
scala passwordManager.scala
```

A sample run of the program would look like this:

```
What is the file that contains your passwords? Give the filename with extension:
> Presentation\data.txt
Would you like to read existing passwords or write an additional password? ("read" or "write")
> read
The file contains passwords for the following:
1: Email
2: Bank
3: Facebook
Please put the number of the password you want to decode.
> 1
Please input the 128-bit (16 characters) key that was used to encrypt this password.
> 0123456789101112
The username is: matt
The password is: password
Do you want to do anything else? ("yes" to continue)
> no
```

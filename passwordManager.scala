import scala.io.StdIn._
import scala.io.Source
import java.io.FileWriter
import java.io.PrintWriter
import java.nio.file.Paths
import java.nio.file.Files
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.crypto.BadPaddingException

val cipher:Cipher = Cipher.getInstance("AES")
val filename = getFilename

var promptAgain = true

do {
    mainBehavior
    println("Do you want to do anything else? (\"yes\" to continue)")
    val answer = readLine
    if (answer.toUpperCase == "YES") {
        promptAgain = true
    }
    else {
        promptAgain = false
    }
} while (promptAgain)

def mainBehavior() {
    println("Would you like to read existing passwords or write an additional password? (\"read\" or \"write\")")
    val choice = readLine
    if (choice.toUpperCase == "READ") readFile
    else if (choice.toUpperCase == "WRITE") writeFile
    else {
        println("That is not a valid choice. Please put either \"read\" or \"write\".")
        mainBehavior
    }
}

def readFile() {

    // iterator of chars
    val file = Source.fromFile(filename)

    // lines of a file as array of strings
    val lines = file.reset.getLines.toArray

    file.close

    // an array of paired strings values
    // an element in this array is Array(username, password)
    val pairs = Array.tabulate(lines.length)(index => lines(index).split(":")).zipWithIndex

    if (pairs.length != 0) {
        println("The file contains passwords for the following:")
        for (line <- pairs) {
            println(s"${line._2 + 1}: ${line._1(0)}")
        }
        
        val index = getValidIndex(1, pairs.length+1) - 1

        println("Please input the 128-bit (16 characters) key that was used to encrypt this password.")
        val key = keyConfig

        val (user, password) = decrypt(pairs(index)._1(1), pairs(index)._1(2), key).getOrElse(None, None)

        if (user != None && password != None) {
            println(s"The username is: $user")
            println(s"The password is: $password")
        }
    }
    else {
        println("You have no passwords in this file!")
    }

}

def writeFile() {
    val writer = new PrintWriter(new FileWriter("data.txt", true))

    println("Please input the 128-bit (16 characters) key that will be used to encrypt this password.")
    println("You will need this key to decrypt your password, so write it down somewhere and don\'t forget it!")
    val key = keyConfig

    println("Input a tag to identify this password:")
    val tag = readLine
    
    println("Input a username:")
    val userBytes = encrypt(readLine, key)
    var user = ""
    userBytes.foreach(user += _ + " ")
    user = user.slice(0, user.length - 1)

    println("Input a password:")
    val passwordBytes = encrypt(readLine, key)
    var password = ""
    passwordBytes.foreach(password += _ + " ")
    password = password.slice(0, password.length - 1)

    writer.println(s"$tag:$user:$password")

    writer.close
    println(s"Password successfully written to $filename!")
}

def encrypt(message : String, key : SecretKey) : Array[Byte] = {
    // initialize the cipher to encrypt using the key, then give it the array of bytes to encrypt
    cipher.init(Cipher.ENCRYPT_MODE, key)
    cipher.doFinal(message.getBytes())
}

def decrypt(nameCode : String, pwCode : String, key : SecretKey) : Option[(String, String)] = {
    // convert the strings from the text files to arrays of bytes
    val nameBytes : Array[Byte] = Array.fill(nameCode.split(" ").size)(0)
    val nameRaw : Array[(String, Int)] = nameCode.split(" ").zipWithIndex
    nameRaw.foreach(b => nameBytes(b._2) = b._1.toByte)
    val pwBytes : Array[Byte] = Array.fill(pwCode.split(" ").size)(0)
    val pwRaw : Array[(String, Int)] = pwCode.split(" ").zipWithIndex
    pwRaw.foreach(b => pwBytes(b._2) = b._1.toByte)
    // initialize the cipher to decrypt using the key, then give it both arrays of bytes to decrypt
    // if the key is incorrect, it will throw the exception and fail to decrypt
    cipher.init(Cipher.DECRYPT_MODE, key)
    try {
        val nameDecrypted = cipher.doFinal(nameBytes)
        val pwDecrypted = cipher.doFinal(pwBytes)
        val name = new String(nameDecrypted)
        val pw = new String(pwDecrypted)
        Some(name, pw)
    }
    catch {
        case e : BadPaddingException => {
            println("The key you input was incorrect.")
            None
        }
    }
}

def getFilename() : String = {
    println("What is the file that contains your passwords? Give the filename with extension:")
    val filename = readLine
    if (Files.exists(Paths.get(filename))) {
        filename
    }
    else {
        println("No such file exists in this directory. Make sure you have created a file.")
        getFilename
    }
}

def keyConfig() : SecretKey = {
    getValidKey
}

def getValidKey() : SecretKey = {
    val key = readLine
    if (key.getBytes.size == 16) {
        new SecretKeySpec(key.getBytes(), "AES")
    }
    else {
        println("Invalid key; please input a valid key.")
        getValidKey
    }
}

def getValidIndex(start : Int, end : Int) : Int = {
    try {
        println("Please put the number of the password you want to decode.")
        val index = readInt
        if (index < start || index >= end) {
            println("There is not a password for that number.")
            getValidIndex(start, end)
        }
        else {
            index
        }
    }
    catch {
        case e : NumberFormatException => {
            println("That is not a number.")
            getValidIndex(start, end)
        }
    }
    
}
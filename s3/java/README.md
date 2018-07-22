
# DownloadEncryptedFile #

* Refer downloadencryptedfile.png
* We might need security policy jar in jdk directory
* create two folders aws-java-sdk and lib
* Compile

    ` javac -cp ".;/aws-java-sdk/1.10.2/lib/*;./lib/*" DownloadEncryptedFile.java`
* Run

    ` java -cp ".;/aws-java-sdk/1.10.2/lib/*;./lib/*" DownloadEncryptedFile s3://<bucket_name>/<file_name> <symmKey> <Local_output_path> <env>`

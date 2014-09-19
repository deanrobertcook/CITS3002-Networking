Compiling instructions 

Please ensure that the folder structure is maintained in the project folder.
The root folder is the Submit/ folder
the client folder is root/client, and all client files should be saved in root/client/files

similarly, the server folder is root/server, and all server files will be saved and retrieved from root/server/files.

Start the server first
java server/SSLServer

Open a new cmd window, then start the client 
java client/ClientStarter -args


Running the client with the -r command will mean that the client does not shutdown after the completion of the first protocol.
Instead, it will wait for another command line argument. This allows it to remember which host it connected to (say when using the
-h command) and what the minimum trust ring circumference should be (using the -c command). That is, once set, -h and -c will remain
for the life of the program, but can be changed at any time in between protocols using -h or -c again.

Commands:

-r: set the client to continue running after each command is processed. The user will be prompted for further commands.

-a filename: upload a file to the server

-f filename: retrieve a file from the server
-c circ -f filename: retrieve a file from the server only if the ring of trust is at least as great as circ
alternatively, -c can be set at any time in a persistant client and will affect all subsequent calls to -f.

-h address:port [and any other command] : connect to a server located at IP address address and port port.
alternatively, -h can be set at any time in a persistant client and the client will attempt all subsequent commands with the specified
server

-l : retrieve all files on the server
-c circ -l : retrieve all files on the server that have a trust ring at least as great as circ
alternatively, -c can be set at any time in a persistant client and will affect all subsequent calls to -l.

-u certName[no ext] : upload a certificate to the server. All certificates should be of the .cert format, but the extension should be omitted.

-v fileName certName[no ext] : vouch for a file (or cert, include extension if first argument) with a certificate certName, but the extensiion should
be omitted for the second argument. 

exit : exit a persistent client. 
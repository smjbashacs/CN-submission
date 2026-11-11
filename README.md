# SockTalk

A multi-client group chat application built with plain Java TCP sockets (no external libraries).

**Features:** register/login, join or leave groups (CSE, ECE, EEE, MECH), group messaging, message history. Data is stored in flat files under `src/serverpackage/DataSource/`.

**Run** (from the `Group Chat Application` folder):
1. Compile: `javac -d bin src/serverpackage/*.java src/clientpackage/*.java`
2. Start server (port 3410): `java -cp bin serverpackage.ServerMain`
3. Start a client in another terminal: `java -cp bin clientpackage.ChatClient`

top-link
========

[![Build Status](https://travis-ci.org/wsky/top-link.png?branch=master)](https://travis-ci.org/wsky/top-link)

embedded duplex multi-channel endpoint and connection management for c#/java/...

https://gist.github.com/4680940

[More](doc/arch.md)

![coverage](https://raw.github.com/wsky/top-link/master/doc/coverage.png)

## Build

dev version 
- java v1.1.1 https://github.com/wsky/top-link/issues?direction=desc&milestone=4&page=1&sort=updated&state=open
- c# v1.0 https://github.com/wsky/top-link/issues?direction=desc&milestone=5&page=1&sort=updated&state=open

set external repo first
```
git submodule init
git submodule update
git submodule foreach git pull
```

Java
```bash
cd java
mvn package
```

or

```shell
build_java.ps1
```

custom package:
```shell
cd java
mvn -DgroupId=yourDomain -DartifactId=yourId -DfinalName=jarName -DdateVersion=-20130802 clean package
```

C#

clone tool repo first, git://github.com/wsky/work-tool.git

```c#
cd csharp
build.bat
build.bat test
```

## Endpoint

Application exposure in a node on the network. They can talk(RTT based) with each other.

[More](doc/endpoint.md)

## Build-in RPC

- Low-Level implementation to support application extension.
- High-Level Abstract Remoting
- Cross-Language support, Java/C#/...
	- support interop with .net remoting
- Multi-Transport, tcp/http/websocket

[More](doc/remoting.md)

## License

- Netty, Apache License Version 2.0

	https://github.com/netty/netty

	https://github.com/netty/netty/blob/master/LICENSE.txt

- RemotingProtocolParser, MIT License

	https://github.com/wsky/RemotingProtocolParser

	https://github.com/wsky/RemotingProtocolParser/blob/master/README.md#license

- websocket-client, MIT License

	https://github.com/wsky/websocket-client/tree/top-link

	https://github.com/wsky/websocket-client/blob/top-link/LICENSE

	Base64, Licence (BSD)

	Copyright (c) 2004, Mikael Grev, MiG InfoCom AB. (base64 @ miginfocom . com) All rights reserved.

- websocket-sharp, MIT License

	https://github.com/sta/websocket-sharp

	https://github.com/wsky/websocket-sharp/tree/net20

	http://www.opensource.org/licenses/mit-license.php

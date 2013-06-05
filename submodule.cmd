#git submodule add git://github.com/wsky/RemotingProtocolParser.git java/external/RemotingProtocolParser
#git submodule add git://github.com/wsky/websocket-client.git java/external/websocket-client
#git checkout top-link
#git submodule add git://github.com/wsky/websocket-sharp.git csharp/external/websocket-sharp
#git checkout net20

git submodule init
git submodule update
git submodule foreach git pull
using System;
using System.Collections.Generic;
using System.IO;
using System.Net.Sockets;
using System.Text;
using Top.Link.Channel.TCP;
using Top.Link.Remoting.Protocol;
using Top.Link.Remoting.Serialization;
using Top.Link.Remoting.Serialization.Json;

namespace Top.Link.Remoting
{
    public class TcpRemotingHelper
    {
        private static readonly ISerializer Serializer = new CrossLanguageJsonSerializer();

        /// <summary>do io loop with tcpclient
        /// </summary>
        /// <param name="log"></param>
        /// <param name="server"></param>
        /// <param name="tcpClient"></param>
        public static void IOWorker(ILog log, TcpServerChannel server, TcpClient tcpClient)
        {
            //remoting core protocol
            //Preamble 4
            //MajorVersion 1
            //MinorVersion 1
            //ReadOperation 2
            //ReadContentDelimiter 2
            //ReadContentLength 4
            var headerLength = 4 + 1 + 1 + 2 + 2 + 4;
            var header = new byte[headerLength];
            NetworkStream stream = tcpClient.GetStream();
            stream.BeginRead(header, 0, header.Length, o =>
            {
                RemotingTcpProtocolHandle handle = null;
                try
                {
                    var length = stream.EndRead(o);
                    handle = length == headerLength ? Parse(header, stream) : null;
                }
                catch (Exception e)
                {
                    handle = null;
                    log.Error(e);
                }
                finally
                {
                    Process(handle, log, server, tcpClient);
                }
            }, null);
        }
        private static RemotingTcpProtocolHandle Parse(byte[] header, NetworkStream stream)
        {
            var handle = new RemotingTcpProtocolHandleWrapper(new MemoryStream(header));
            handle.ReadPreamble();
            handle.ReadMajorVersion();
            handle.ReadMinorVersion();
            handle.ReadOperation();
            handle.ReadContentDelimiter();
            handle.ReadContentLength();
            handle.Source = stream;
            return handle;
        }
        private static void Process(RemotingTcpProtocolHandle handle, ILog log, TcpServerChannel server, TcpClient tcpClient)
        {
            try
            {
                if (handle == null)
                    return;
                //TODO:process methodCall
                MethodCall methodCall = Serializer.DeserializeMethodCall(handle.ReadContent());
            }
            catch (Exception e)
            {
                log.Error(e);
                //TODO:return error to client
            }
            finally
            {
                IOWorker(log, server, tcpClient);
            }
        }

        class RemotingTcpProtocolHandleWrapper : RemotingTcpProtocolHandle
        {
            public Stream Source { set { this._source = value; } }
            public RemotingTcpProtocolHandleWrapper(Stream source) : base(source) { }
        }
    }
}
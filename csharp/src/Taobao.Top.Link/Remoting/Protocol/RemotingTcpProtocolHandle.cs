using System;
using System.Collections.Generic;
using System.IO;
using System.Text;
using RemotingProtocolParser.TCP;

namespace Taobao.Top.Link.Remoting.Protocol
{
    /// <summary>extend remoting protocol
    /// </summary>
    internal class RemotingTcpProtocolHandle : TcpProtocolHandle
    {
        public RemotingTcpProtocolHandle(Stream source) : base(source) { }

        protected override bool ReadExtendedHeader(ushort headerType, IDictionary<string, object> dict)
        {
            if (headerType == RemotingTcpHeaders.Flag)
            {
                this.ReadByte();
                dict.Add(RemotingTransportHeader.Flag, this.ReadInt32());
                return true;
            }
            if (headerType == RemotingTcpHeaders.Format)
            {
                this.ReadByte();
                dict.Add(RemotingTransportHeader.Format, this.ReadCountedString());
                return true;
            }
            return false;
        }
        protected override bool WriteExtendedHeader(KeyValuePair<string, object> item)
        {
            if (item.Key.Equals(RemotingTransportHeader.Flag, StringComparison.InvariantCultureIgnoreCase))
            {
                this.WriteUInt16(RemotingTcpHeaders.Flag);
                this.WriteByte(TcpHeaderFormat.Int32);
                this.WriteInt32((int)item.Value);
                return true;
            }
            if (item.Key.Equals(RemotingTransportHeader.Format, StringComparison.InvariantCultureIgnoreCase))
            {
                if (item.Value != null)
                {
                    this.WriteUInt16(RemotingTcpHeaders.Format);
                    this.WriteByte(TcpHeaderFormat.CountedString);
                    this.WriteCountedString(item.Value.ToString());
                }
                return true;
            }
            return false;
        }
    }
}
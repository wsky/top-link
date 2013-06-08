using System;
using System.Collections.Generic;
using System.IO;
using System.Text;

namespace Taobao.Top.Link.Endpoints
{
    //simple protocol impl
    public class MessageIO
    {
        public static Message ReadMessage(Stream input)
        {
            var buffer = new BinaryReader(input);
            Message msg = new Message();
            msg.MessageType = buffer.ReadInt16();
            // read kv
            IDictionary<string, string> dict = new Dictionary<string, string>();
            short headerType = buffer.ReadInt16();
            while (headerType != MessageType.HeaderType.EndOfHeaders)
            {
                if (headerType == MessageType.HeaderType.Custom)
                    dict.Add(ReadCountedString(buffer), ReadCountedString(buffer));
                else if (headerType == MessageType.HeaderType.StatusCode)
                    msg.StatusCode = buffer.ReadInt32();
                else if (headerType == MessageType.HeaderType.StatusPhrase)
                    msg.StatusPhase = ReadCountedString(buffer);
                else if (headerType == MessageType.HeaderType.Flag)
                    msg.Flag = buffer.ReadInt32();
                else if (headerType == MessageType.HeaderType.Token)
                    msg.Token = ReadCountedString(buffer);
                headerType = buffer.ReadInt16();
            }
            msg.Content = dict;
            return msg;
        }
        public static void WriteMessage(Stream input, Message message)
        {
            var buffer = new BinaryWriter(input);
            buffer.Write(message.MessageType);
            if (message.StatusCode > 0)
            {
                buffer.Write(MessageType.HeaderType.StatusCode);
                buffer.Write(message.StatusCode);
            }
            if (message.StatusPhase != null && message.StatusPhase != "")
            {
                buffer.Write(MessageType.HeaderType.StatusPhrase);
                WriteCountedString(buffer, message.StatusPhase);
            }
            if (message.Flag > 0)
            {
                buffer.Write(MessageType.HeaderType.Flag);
                buffer.Write(message.Flag);
            }
            if (message.Token != null && message.Token != "")
            {
                buffer.Write(MessageType.HeaderType.Token);
                WriteCountedString(buffer, message.Token);
            }
            if (message.Content != null)
            {
                foreach (var i in message.Content)
                    WriteCustomHeader(buffer, i.Key, i.Value);
            }
            buffer.Write(MessageType.HeaderType.EndOfHeaders);
        }
        // UTF-8 only
        private static string ReadCountedString(BinaryReader buffer)
        {
            int size = buffer.ReadInt32();
            return size > 0 ? Encoding.UTF8.GetString(buffer.ReadBytes(size)) : null;
        }
        private static void WriteCountedString(BinaryWriter buffer, string value)
        {
            int strLength = 0;
            if (value != null)
                strLength = value.Length;

            if (strLength > 0)
            {
                byte[] strBytes = Encoding.UTF8.GetBytes(value);
                buffer.Write(strBytes.Length);
                buffer.Write(strBytes);
            }
            else
                buffer.Write(0);
        }
        private static void WriteCustomHeader(BinaryWriter buffer, string name, string value)
        {
            buffer.Write(MessageType.HeaderType.Custom);
            WriteCountedString(buffer, name);
            WriteCountedString(buffer, value);
        }
    }
}
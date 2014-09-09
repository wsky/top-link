using System;
using System.Collections.Generic;
using System.Text;

namespace Top.Link.Remoting
{
    public class RemotingException : LinkException
    {
        public RemotingException() : base(string.Empty) { }
        public RemotingException(string message) : base(message, null) { }
        public RemotingException(string message, Exception innerException) : base(0, message, innerException) { }
        public RemotingException(int errorCode, string message) : base(0, message, null) { }
        public RemotingException(int errorCode, string message, Exception innerException) : base(errorCode, message, innerException) { }
    }
}

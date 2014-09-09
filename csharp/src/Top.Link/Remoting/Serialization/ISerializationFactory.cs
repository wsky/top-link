using System;
using System.Collections.Generic;
using System.Text;

namespace Top.Link.Remoting.Serialization
{
    public interface ISerializationFactory
    {
        ISerializer Get(object format);
    }
}
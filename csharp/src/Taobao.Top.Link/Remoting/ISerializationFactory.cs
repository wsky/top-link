using System;
using System.Collections.Generic;
using System.Text;

namespace Taobao.Top.Link.Remoting
{
    public interface ISerializationFactory
    {
        ISerializer Get(object format);
    }
}
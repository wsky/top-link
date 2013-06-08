using System;
using System.Collections.Generic;
using System.Text;

namespace Taobao.Top.Link.Endpoints
{
    /// <summary>simple id with Name
    /// </summary>
    public class SimpleIdentity : Identity
    {
        public string Name { get; private set; }

        public SimpleIdentity(string name)
        {
            this.Name = name;
        }

        public Identity Parse(object data)
        {
            return new SimpleIdentity((data as IDictionary<String, String>)["name"]);
        }

        public void Render(object to)
        {
            (to as IDictionary<String, String>).Add("name", this.Name);
        }

        public bool Equals(Identity id)
        {
            return this.Name.Equals((id as SimpleIdentity).Name);
        }
    }
}
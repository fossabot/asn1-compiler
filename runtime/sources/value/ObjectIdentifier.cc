#include <value/ObjectIdentifier.hh>
#include <common/Utils.hh>
#include <common/ASN1Exception.hh>

namespace asn1
{

// Parses and sets oid
void ObjectIdentifier::_parseAndSetOid(const std::string& value)
{
   std::vector<std::string> tokens;
   
   utils::split(value, tokens, ".");
   if (tokens.size() <= 0)
      throw ASN1Exception("Object Identifier value '" + value + "' is incorrect");

   for (std::vector<std::string>::const_iterator p = tokens.begin(); p != tokens.end(); ++p)
      _oid.push_back(utils::ston<uint16_t>(*p)); // TODO: take into case when conversation is not possible
}

// Returns object identifier represented as string
std::string ObjectIdentifier::getValueAsString() const
{
   std::string value;

   bool isFirst(true);
   for (ObjectIdentifierRaw::const_iterator p = _oid.begin(); p != _oid.end(); ++p)
   {
      if (isFirst)
      {
         value = utils::ntos(*p);
         isFirst = false;
      }
      else
         value += "." + utils::ntos(*p);
   }

   return value;
}

}

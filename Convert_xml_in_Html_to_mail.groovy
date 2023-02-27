import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import java.text.DecimalFormat;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.lang.String;

/* --source paylaod--
<?xml version="1.0" encoding="UTF-8"?>
<root>
    <DateTime>${header.currentDate}</DateTime>
    <SiteId>${header.idSite}</SiteId>
    <ParentNode>
        <Childs>
            <ItemId>101</ItemId>
            <ItemDescription>Texte de descritopn du produit 101</ItemDescription>
            <Quantity1>250</Quantity1>
            <Quantity2>130</Quantity2>
            <QuantityDifference>120</QuantityDifference>
        </Childs>
        <Childs>
            <ItemId>102</ItemId>
            <ItemDescription>Texte de descritopn du produit 102</ItemDescription>
            <Quantity1>80</Quantity1>
            <Quantity2>20</Quantity2>
            <QuantityDifference>60</QuantityDifference>
        </Childs>
    </ParentNode>
</root>
*/

//date formating -- (format source : 2023-02-21T10:34:11)
def String FormatDate(String currentDate, String initDate){
    def datePattern = "yyyy-MM-dd";
    def dateString = currentDate.substring(0,10);
    def Date dateConverted = Date.parse(datePattern, dateString);
    def year = dateConverted.getYear()+1900;
    def month = dateConverted.getMonth()+1 ;
    if(month<10){
        month = "0"+month
    }
    def day = dateConverted.getDate();
    return initDate = day+"/"+ month +"/"+year+" - "+ currentDate.substring(11,13)+"h"+ currentDate.substring(14,16)+"min";
}

def Message processData(Message message){

    //get and parse payload
    def body = message.getBody(java.lang.String) as String;

    def parseBody = new XmlSlurper().parseText(body);

    //get headers values
    def headers = message.getHeaders();
    def idSite = headers.get("idSite");
    def currentDate = headers.get("currentDate");

    //create custom const
    def arrayHeader = '';
    def arrayBodyHtml ='';
    def arrayBodyRows = '';
    def numberColumn = 0;
    def concatTemplateBody = '';
    def initDate = '';
    def arrayInMail = parseBody.ParentNode.Childs;

    //formating date
    formatedDate = FormatDate(currentDate,initDate);

    //construc arrayTitle
    def arrayTitle = "Relevés de stocks au $formatedDate sur le site $idSite";

    //construc arrayHeader
    arrayInMail[0].each{it -> 
    it.children().each { child ->
    arrayHeader = arrayHeader + "<th>"+child.name()+"</th>"
    numberColumn++;
    }}

    //construct arrayBody
    arrayInMail.each{it -> 
    it.children().each { child ->
    arrayBodyRows = arrayBodyRows + "<td>"+child.text()+"</td>"
    }
    arrayBodyHtml = arrayBodyHtml + "<tr>"+arrayBodyRows+"</tr>";
    arrayBodyRows = '';
    }

    //construct mail body
    def templateHtml = """
    <table width="80%" border="1" align="center" border-collapse="collapse">
        <thead>
            <tr>
                <th bgcolor="#add8e6" colspan="$numberColumn">$arrayTitle</th>  
            </tr>
            <tr>
                $arrayHeader  
            </tr>
        </thead>
        <tbody align="center">
            $arrayBodyHtml
        </tbody>
    </table>""";

    message.setBody(templateHtml);

    return message;
}
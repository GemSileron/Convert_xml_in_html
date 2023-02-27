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


//array rowscConstructor
def String arrayRowsTemplate (String columnOne, String columnTwo, String columnThree, String columnFour,  String columnFive) {
    return "<tr><td>"+columnOne+"</td><td>"+columnTwo+"</td><td>"+columnThree+"</td><td>"+columnFour+"</td><td>"+columnFive+"</td></tr>";
}

//date formating -- (format source : 2023-02-21T10:34:11)
def String FormatDate(String currentDate, String formatedDate){
    def datePattern = "yyyy-MM-dd";
    def dateString = currentDate.substring(0,10);
    def Date dateConverted = Date.parse(datePattern, dateString);
    def year = dateConverted.getYear()+1900;
    def month = dateConverted.getMonth()+1 ;
    if(month<10){
        month = "0"+month
    }
    def day = dateConverted.getDate();
    return formatedDate = day+"/"+ month +"/"+year+" - "+ currentDate.substring(11,13)+"h"+ currentDate.substring(14,16)+"min";
}


def Message processData(Message message) {

    //get and parse payload
    def body = message.getBody(java.lang.String) as String;
    def parseBody = new XmlSlurper().parseText(body);

    //get headers values
    def headers = message.getHeaders();
    def idSite = headers.get("idSite");
    def currentDate = headers.get("currentDate");
    def arrayColumnNumber = headers.get("array_column_number");
    def arrayColumnName1 = headers.get("array_column_1_name");
    def arrayColumnName2 = headers.get("array_column_2_name");
    def arrayColumnName3 = headers.get("array_column_3_name");
    def arrayColumnName4 = headers.get("array_column_4_name");
    def arrayColumnName5 = headers.get("array_column_5_name");

    //create custom const
    def concatTemplateBody = '';
    def formatedDate = '';
    def arrayInMail = parseBody.ParentNode.Childs;
    def arrayTitle = "Relevés de stocks au $formatedDate";

    //formating date
    formatedDate = FormatDate(currentDate,formatedDate);

    //construct array
    arrayInMail.each { it ->
        concatTemplateBody = concatTemplateBody + arrayRowsTemplate(it.ItemId.toString(), it.ItemDescription.toString(), it.Quantity2.toString(), it.Quantity1.toString(), it.QuantityDifference.toString());
    }

    //construct mail body
    def templateHtml = """
    <table width="80%" border="1" align="center" border-collapse="collapse">
        <thead>
            <tr>
                <th bgcolor="#add8e6" colspan="$arrayColumnNumber">$arrayTitle</th>  
            </tr>
            <tr>
                <th bgcolor="#add8e6">$arrayColumnName1</th>
                <th bgcolor="#add8e6">$arrayColumnName2</th>
                <th bgcolor="#add8e6">$arrayColumnName3</th>
                <th bgcolor="#add8e6">$arrayColumnName4</th>
                <th bgcolor="#add8e6">$arrayColumnName5</th>
            </tr>
        </thead>
        <tbody align="center">
           $concatTemplateBody
        </tbody>
    </table>""";

    message.setBody(templateHtml);

    return message;
}
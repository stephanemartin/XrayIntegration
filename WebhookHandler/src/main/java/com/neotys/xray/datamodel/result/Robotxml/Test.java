package com.neotys.xray.datamodel.result.Robotxml;

import com.neotys.ascode.swagger.client.model.*;
import com.neotys.xray.HttpResult.NeoLoadTestContext;
import com.neotys.xray.common.NeoLoadUtils;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.neotys.xray.conf.Constants.SLA_TYPE_PERINTERVAL;
import static com.neotys.xray.conf.Constants.SLA_TYPE_PERTEST;
@XmlRootElement
public class Test {

    //[
    //  {
    //    "kpi": "avg-transaction-resp-time",
    //    "status": "PASSED",
    //    "value": 0.3087143,
    //    "warningThreshold": {
    //      "operator": ">=",
    //      "value": 4
    //    },
    //    "failedThreshold": {
    //      "operator": ">=",
    //      "value": 7
    //    },
    //    "element": {
    //      "elementId": "8dbff581-f7d7-4dc1-98f3-b2a77061d82a",
    //      "name": "return to reports page",
    //      "category": "TRANSACTION",
    //      "userpath": "BrowserUser_Create_report",
    //      "parent": "Actions"
    //    }
    //  },
    //  {
    //    "kpi": "avg-transaction-resp-time",
    //    "status": "PASSED",
    //    "value": 1.668,
    //    "warningThreshold": {
    //      "operator": ">=",
    //      "value": 4
    //    },
    //    "failedThreshold": {
    //      "operator": ">=",
    //      "value": 7
    //    },
    //    "element": {
    //      "elementId": "339eb312-2a5a-44af-bb55-48176fc8c2a2",
    //      "name": "submit report",
    //      "category": "TRANSACTION",
    //      "userpath": "BrowserUser_Create_report",
    //      "parent": "Actions"
    //    }
    //  },
    //  {
    //    "kpi": "avg-transaction-resp-time",
    //    "status": "PASSED",
    //    "value": 0.96914285,
    //    "warningThreshold": {
    //      "operator": ">=",
    //      "value": 4
    //    },
    //    "failedThreshold": {
    //      "operator": ">=",
    //      "value": 7
    //    },
    //    "element": {
    //      "elementId": "eb1cee2c-2f37-43f7-a2bd-92cc6990f92f",
    //      "name": "submit",
    //      "category": "TRANSACTION",
    //      "userpath": "BrowserUser_Create_report",
    //      "parent": "Try"
    //    }
    //  },
    //  {
    //    "kpi": "avg-transaction-resp-time",
    //    "status": "PASSED",
    //    "value": 1.5881429,
    //    "warningThreshold": {
    //      "operator": ">=",
    //      "value": 4
    //    },
    //    "failedThreshold": {
    //      "operator": ">=",
    //      "value": 7
    //    },
    //    "element": {
    //      "elementId": "f18b10f8-9f51-44ac-b17d-22eed776ab27",
    //      "name": "Home",
    //      "category": "TRANSACTION",
    //      "userpath": "BrowserUser_Create_report",
    //      "parent": "Actions"
    //    }
    //  }
    //]

    private int id;

    private String name;
    private List<KW> kw;
    private Optional<Status> status;
    private Optional<Tags> tags;

    public Test(int id, String name, List<KW> kw, Optional<Status> status, Optional<Tags> tags) {
        this.id = id;
        this.name = name;
        this.kw = kw;
        this.status = status;
        this.tags = tags;
    }

    public Test()
    {

    }
    public Test(NeoLoadTestContext context)
    {
        this.id=2;
        this.name=context.getScenarioName();
        this.kw=new ArrayList<>();
        String startdate= NeoLoadUtils.convertDateLongToString(context.getTeststart());
        String endate=NeoLoadUtils.convertDateLongToString(context.getTestEnd());

        //----transform the NL SLA into KW-------------
        if(context.getArrayOfSLAGlobalIndicatorDefinitionOptional().isPresent())
            addGlobalSLAIndicators(startdate,endate,context.getArrayOfSLAGlobalIndicatorDefinitionOptional().get());
        if(context.getArrayOfSLAPerTestDefinition().isPresent())
            addSLAPerTest(startdate,endate,context.getArrayOfSLAPerTestDefinition().get());
        if(context.getArrayOfSLAPerIntervalDefinition().isPresent())
            addSLAPerInterval(startdate,endate,context.getArrayOfSLAPerIntervalDefinition().get());

        Status status=new Status(context.getStatus(),startdate,endate,Optional.empty());
        setStatus(Optional.of(status));

        List<String> taglist =new ArrayList<>();
        taglist.add("neoload");
        taglist.add("performance");

        if(context.getDescription().getTags().isPresent())
        {
            taglist.addAll(context.getDescription().getTags().get().stream().collect(Collectors.toList()));
        }
        Tags tags=new Tags();
        tags.setTag(taglist);
        setTags(Optional.ofNullable(tags));

    }

    private void addGlobalSLAIndicators(String start,String end,ArrayOfSLAGlobalIndicatorDefinition arrayOfSLAGlobalIndicatorDefinition)
    {
        arrayOfSLAGlobalIndicatorDefinition.forEach(slaGlobalIndicatorDefinition -> {
            KW indicator=new KW();
            if(slaGlobalIndicatorDefinition.getKpi()!=null)
                indicator.setName("GLOBAL_"+slaGlobalIndicatorDefinition.getKpi().getValue());
            else
                indicator.setName("GLOBAL_");
            indicator.setStatus(new Status(slaGlobalIndicatorDefinition.getStatus().getValue(),start,end, Optional.empty()));
            String level;
            if(slaGlobalIndicatorDefinition.getStatus().getValue().equalsIgnoreCase("PASSED"))
                level="INFO";
            else
                level="ERROR";
            if(slaGlobalIndicatorDefinition.getKpi()!=null)
                indicator.setMsg(Optional.of(new MSG(level,slaGlobalIndicatorDefinition.getKpi().getValue() +" equal to " + slaGlobalIndicatorDefinition.getValue().toString() +" Failed Thershold is "+ slaGlobalIndicatorDefinition.getFailedThreshold().toString())));
            else
                indicator.setMsg(Optional.of(new MSG(level,"global sla equal to " + slaGlobalIndicatorDefinition.getValue().toString() +" Failed Thershold is "+ slaGlobalIndicatorDefinition.getFailedThreshold().toString())));

            kw.add(indicator);
        });
    }
    @XmlElement(required = false)
    public Status getStatus() {
        if(status.isPresent())
            return status.get();
        else
            return null;
    }



    public void setStatus(Optional<Status> status) {
        this.status = status;
    }

    @XmlElement( name="tags",required = false)
    public Tags getTags() {
        if(tags.isPresent())
            return tags.get();
        else
            return null;
    }

    public void setTags(Optional<Tags> tags) {
        this.tags = tags;
    }

    private String generateKWname(SLAElementDefinition definition, SLAKPIDefinition kpiname, String type)
    {
        if(kpiname!=null)
            return type+"."+definition.getName().replaceAll(" ","_")+"."+definition.getCategory().getValue()+"."+kpiname.getValue();
        else
            return type+"."+definition.getName().replaceAll(" ","_")+"."+definition.getCategory().getValue();

    }
    private void addSLAPerTest(String start,String end,ArrayOfSLAPerTestDefinition arrayOfSLAPerTestDefinition)
    {

        arrayOfSLAPerTestDefinition.forEach(slaPerTestDefinition -> {
                    KW indicator=new KW();

                    indicator.setName(generateKWname(slaPerTestDefinition.getElement(),slaPerTestDefinition.getKpi(),SLA_TYPE_PERTEST));
                    indicator.setStatus(new Status(slaPerTestDefinition.getStatus().getValue(),start,end, Optional.empty()));
                    String level;
                    if(slaPerTestDefinition.getStatus().getValue().equalsIgnoreCase("PASSED"))
                        level="INFO";
                    else
                        level="ERROR";


                    indicator.setMsg(Optional.ofNullable(generateMessage(slaPerTestDefinition.getFailedThreshold(),slaPerTestDefinition.getValue(),slaPerTestDefinition.getElement(),slaPerTestDefinition.getKpi(),level)));
                    kw.add(indicator);
                }

                );
    }

    private MSG generateMessage(ThresholdDefinition definition,Float value,SLAElementDefinition elementDefinition,SLAKPIDefinition slakpiDefinition,String level)
    {
        if(slakpiDefinition !=null)
            return new MSG(level,elementDefinition.getCategory().getValue() + " with the name "+ elementDefinition.getName()+ " the kpi :"+ slakpiDefinition.getValue() +" equal to " + String.valueOf(value) +" - the Failed Threshold is "+ definition.toString());
        else
            return new MSG(level,elementDefinition.getCategory().getValue() + " with the name "+ elementDefinition.getName()+ " sla  equal to " + String.valueOf(value) +" - the Failed Threshold is "+ definition.toString());

    }
    private void addSLAPerInterval(String start,String end,ArrayOfSLAPerIntervalDefinition arrayOfSLAPerIntervalDefinition)
    {
        arrayOfSLAPerIntervalDefinition.forEach(slaPerTestDefinition -> {
                    KW indicator=new KW();

                    indicator.setName(generateKWname(slaPerTestDefinition.getElement(),slaPerTestDefinition.getKpi(),SLA_TYPE_PERINTERVAL));
                    indicator.setStatus(new Status(slaPerTestDefinition.getStatus().getValue(),start,end, Optional.empty()));
                    String level;
                    if(slaPerTestDefinition.getStatus().getValue().equalsIgnoreCase("PASSED"))
                        level="INFO";
                    else
                        level="ERROR";


                    indicator.setMsg(Optional.ofNullable(generateMessage(slaPerTestDefinition.getFailedThreshold(),slaPerTestDefinition.getFailed(),slaPerTestDefinition.getElement(),slaPerTestDefinition.getKpi(),level)));
                    kw.add(indicator);
                }

        );
    }
    @XmlAttribute
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    @XmlAttribute
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    @XmlElement
    public List<KW> getKw() {
        return kw;
    }


    public void setKw(List<KW> kw) {
        this.kw = kw;
    }
}
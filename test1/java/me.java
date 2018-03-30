package cn.inspower.service.warning.domain;

import java.lang.ref.Reference;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import cn.inspower.data.sync.domain.MeterValue;
import cn.inspower.data.sync.domain.MeterValue4One;
import cn.inspower.resource.baseinfo.dao.IMeasurePointProtectInfoDAO;
import cn.inspower.resource.baseinfo.domain.MeasurePointProtectInfo;
import cn.inspower.resource.configure.domain.MeterChannel;
import cn.inspower.service.warning.domain.base.AbstractWarning;
import cn.inspower.service.warning.service.impl.WarningHelper;
import cn.inspower.service.warning.service.impl.WarningService;
import framework.common.dao.IMongoDBBaseDAO;
import framework.common.tools.ext.ListUtils;
import framework.web.InitExtServlet;
import framework.web.InitServlet;

/**
 * 预警
 * 
 * @author lisixing 2015-11-1933
 */
@Entity
@Table(name = "WG_WARNING")
@NamedQueries({ @NamedQuery(name = "Warning.getWarningList", query = "from Warning t Order by t.getTime"), @NamedQuery(name = "Warning.getWarningListCount", query = "select count(*) from Warning t") })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Warning extends AbstractWarning
{
    public static Logger logger = (Logger) LoggerFactory.getLogger(Warning.class);
    
    private static final long serialVersionUID = 1L;

    /**
     * 正常
     */
    public static String ACTION_1 = "warning1";

    /**
     * 有关内存延迟回收
     */
    @Transient
    public long duration = 0l;
    
    @Transient
    public String address;
    
    @Transient
    public MeterValue meterValue;
    
    //@Transient
    //public Reference<MeterValue> befoeMeterValue;

    @Transient
    private String meterChannelName;
    
    @Transient
    private MeterChannel meterChannel;
    
    @Transient
    private String tempContent;
    
    @Transient
    public String actualValueRemark;
    
    @Transient
    public Float ratingABu;
    
    @Transient
    public Float protectA;
    
    @Transient
    private String eleCustomerID;
    
    @Transient
    private String allXiang;
    
    @Transient
    public Boolean up;
    
    @Transient
    public Boolean down;
    
    public Boolean checkLoadRatio(Float testingValue, Float defaultValue, Boolean iscurrent)
    {
        try
        {
            MongoTemplate mongoTemplate = (MongoTemplate) InitExtServlet.getBean("mongoTemplate4Energy");
            IMongoDBBaseDAO mterValueDAO = (IMongoDBBaseDAO) InitServlet.getBean("meterValue4OneDAO");
            mterValueDAO.setMongoTemplate(mongoTemplate);
            Query query = new Query();
            query.addCriteria(Criteria.where("measurePointId").is(this.getMeasurePointId()).and("sourceType").is(MeterValue.SOURCE_TYPE_1));

            MeterValue4One meterValue = mongoTemplate.findOne(query, MeterValue4One.class);
            MeterValue4One meterValueTemp = null;
            meterValueTemp = meterValue;
            if (meterValueTemp != null)
            {
                if(iscurrent != null && iscurrent)
                {
                    if(meterValueTemp.getAddr_0044() <100)
                    {
                        return false;
                    }
                    if(meterValueTemp.getAddr_0045() <100)
                    {
                        return false;
                    }
                    if(meterValueTemp.getAddr_0046() <100)
                    {
                        return false;
                    }
                }
                Float shiji = meterValueTemp.getApparentPower4One();
                Float x = defaultValue;
                if (x == null)
                {
                    MeasurePointProtectInfo pi = WarningHelper.getInfoByMeasurePointIdAndFeildName(this.getMeasurePointId(), "ratedPower");
                    if(pi != null && pi.getFeildValue() != 0f)
                    {
                        x = pi.getFeildValue();
                    }
                }
                if(shiji/x > testingValue)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
        }
        catch (Exception e)
        {
            return false;
        }
        return false;
    }
    
    public String getAddress()
    {
        return address != null?address.intern():address;
    }

    public void setAddress(String address)
    {
        this.address = address != null?address.intern():address;
    }

    public String getMeterChannelName()
    {
        return meterChannelName != null?meterChannelName.intern():meterChannelName;
    }

    public void setMeterChannelName(String meterChannelName)
    {
        this.meterChannelName = meterChannelName != null?meterChannelName.intern():meterChannelName;
    }

    public String getTempContent()
    {
        return tempContent != null?tempContent.intern():tempContent;
    }

    public void setTempContent(String tempContent)
    {
        this.tempContent = tempContent != null?tempContent.intern():tempContent;
    }

    public MeterChannel getMeterChannel()
    {
        return meterChannel;
    }

    public void setMeterChannel(MeterChannel meterChannel)
    {
        this.meterChannel = meterChannel;
    }
    
    public String getActualValueRemark()
    {
        return actualValueRemark != null?actualValueRemark.intern():actualValueRemark;
    }

    public void setActualValueRemark(String actualValueRemark)
    {
        this.actualValueRemark = actualValueRemark != null?actualValueRemark.intern():actualValueRemark;
    }

    public Float getRatingABu()
    {
        return ratingABu;
    }

    public void setRatingABu(Float ratingABu)
    {
        this.ratingABu = ratingABu;
    }

    public String getEleCustomerID()
    {
        return eleCustomerID != null?eleCustomerID.intern():eleCustomerID;
    }

    public void setEleCustomerID(String eleCustomerID)
    {
        this.eleCustomerID = eleCustomerID != null?eleCustomerID.intern():eleCustomerID;
    }
    
    public WarningCancel toWarningCancel()
    {
        WarningCancel warningCancel = new WarningCancel();
        warningCancel.setActualValueRemark(this.getActualValueRemark());
        warningCancel.setAddress(this.getAddress());
        warningCancel.setAllXiang(this.getAllXiang());
        warningCancel.setDown(this.getDown());
        warningCancel.setDuration(this.getDuration());
        warningCancel.setEleCustomerID(this.getEleCustomerID());
        warningCancel.setMeterChannel(this.getMeterChannel());
        warningCancel.setMeterChannelName(this.getMeterChannelName());
        warningCancel.setProtectA(this.getProtectA());
        warningCancel.setRatingABu(this.getRatingABu());
        warningCancel.setTempContent(this.getTempContent());
        warningCancel.setUp(this.getUp());
        warningCancel.setWarningId(this.getId());
        warningCancel.setAction(this.getAction());
        warningCancel.setActualValueC(this.getActualValueC());
        warningCancel.setAppSignTime(this.getAppSignTime());
        warningCancel.setChanel(this.getChanel());
        warningCancel.setCheckType(this.getCheckType());
        warningCancel.setContent(this.getContent());
        warningCancel.setCustomerId(this.getCustomerId());
        warningCancel.setCustomerName(this.getCustomerName());
        warningCancel.setDelayTime(this.getDelayTime());
        warningCancel.setDisableDate(this.getDisableDate());
        warningCancel.setDownRatio(this.getDownRatio());
        warningCancel.setDownType(this.getDownType());
        warningCancel.setDownValue(this.getDownValue());
        warningCancel.setEnabled(this.getEnabled());
        warningCancel.setFormula(this.getFormula());
        warningCancel.setGetTime(this.getGetTime());
        warningCancel.setInLinkMan(this.getInLinkMan());
        warningCancel.setInLinkManName(this.getInLinkManName());
        warningCancel.setIsAction(this.getIsAction());
        warningCancel.setMeasurePointId(this.getMeasurePointId());
        warningCancel.setMeasurePointName(this.getMeasurePointName());
        warningCancel.setMeterId(this.getMeterId());
        warningCancel.setModifyDate(this.getModifyDate());
        warningCancel.setName(this.getName());
        warningCancel.setOutLinkMan(this.getOutLinkMan());
        warningCancel.setOutLinkManName(this.getOutLinkManName());
        warningCancel.setProtectB(this.getProtectB());
        warningCancel.setRatingA(this.getRatingA());
        warningCancel.setRecordDate(this.getRecordDate());
        warningCancel.setSigner(this.getSigner());
        warningCancel.setType(this.getType());
        warningCancel.setUpRatio(this.getUpRatio());
        warningCancel.setUpType(this.getUpType());
        warningCancel.setUpValue(this.getUpValue());
        warningCancel.setVersion(this.getVersion());
        warningCancel.setWarningConfig(this.getWarningConfig());
        warningCancel.setWarningConfigId(this.getWarningConfigId());
        warningCancel.setWarningRatio(this.getWarningRatio());
        warningCancel.setWarningRatioCondition(this.getWarningRatioCondition());
        return warningCancel;
    }
    
    public void clearWarningCore()
    {
        this.meterValue = null;
        this.setActualValueRemark(null);
        this.setAddress(null);
        this.setEleCustomerID(null);
        this.setMeterChannel(null);
        this.setMeterChannelName(null);
        this.setRatingABu(null);
        this.setTempContent(null);
        this.setAction(null);
        this.setActualValueC(null);
        this.setAppSignTime(null);
        this.setChanel(null);
        this.setCheckType(null);
        this.setContent(null);
        this.setCustomerName(null);
        this.setDisableDate(null);
        //this.setDisabler(null);
        this.setEnabled(null);
        this.setFormula(null);
        this.setGetTime(null);
        this.setId(null);
        this.setIsAction(null);
        this.setMeasurePointId(null);
        this.setMeasurePointName(null);
        this.setMeterId(null);
        //this.setModifier(null);
        this.setModifyDate(null);
        this.setName(null);
        this.setProtectB(null);
        this.setRatingA(null);
        this.setRecordDate(null);
        //this.setRecorder(null);
        this.setType(null);
        this.setVersion(null);
        this.setWarningConfig(null);
        this.setWarningConfigId(null);
        this.setAllXiang(null);
    }

    
    
    public Float getAddr_0046()
    {
        if(meterValue != null && meterValue != null)
        {
            return ((MeterValue4One)meterValue).getAddr_0046();
        }
        return 0f;
    }
    
    public Float getAddr_0045()
    {
        if(meterValue != null && meterValue != null)
        {
            return ((MeterValue4One)meterValue).getAddr_0045();
        }
        return 0f;
    }
    
    public Float getAddr_0044()
    {
        if(meterValue != null && meterValue != null)
        {
            return ((MeterValue4One)meterValue).getAddr_0044();
        }
        return 0f;
    }
    
    public Float getApparentPower()
    {
        if(meterValue != null && meterValue != null)
        {
            return ((MeterValue4One)meterValue).getApparentPower();
        }
        
        return 0f;
    }
    
    public String getAllXiang()
    {
        return allXiang != null?allXiang.intern():allXiang;
    }

    public void setAllXiang(String allXiang)
    {
        this.allXiang = allXiang != null?allXiang.intern():allXiang;
    }
    
    public static ExecutorService CLEAR_POOL = Executors.newFixedThreadPool(1);
    public static void autoClear()
    {
        Timer timer = new Timer("warning Schedule Timer", true);
        TimerTask timerTask = new TimerTask()
        {
            public void run()
            {
                final Runnable sendThread = new Runnable()
                {
                    public void run()
                    {
                        Set<String> s = WarningService.clearWarningMap.keySet();
                        for (String key : s)
                        {
                            Warning warning = WarningService.clearWarningMap.get(key);
                            long x = 0l;
                            if(warning != null)
                            {
                                x = warning.getDuration()*60l*1000l;
                            }
                            else
                            {
                                x = 1000l * 60l * 40l;
                            }
                            if(warning.getTime != null)
                            {
                                x = warning.getTime.getTime()+x+10l*60l*1000l;
                            }
                            else
                            {
                                x = x+5l*60l*1000l;
                            }
                            Date cx = new Date();
                            if(cx.getTime() >= x)
                            {
                                WarningService.clearWarningMap.remove(key);
                            }
                        }
                    }
                };
                CLEAR_POOL.execute(sendThread);
            }
        };
        timer.schedule(timerTask, new Date(), 5l*60l*1000l);
    }

    public long getDuration()
    {
        return duration;
    }

    public void setDuration(long duration)
    {
        this.duration = duration;
    }

    public Float getProtectA()
    {
        return protectA;
    }

    public void setProtectA(Float protectA)
    {
        this.protectA = protectA;
    }
    
    public String getWarningConfigKey()
    {
        WarningConfig warningConfig = this.getWarningConfig();
        if (warningConfig == null)
        {
            logger.error("预警规则不能为空");
            return "";
        }

        return warningConfig.getWarningConfigKey();
    }
    
    public String getWarningConfigUpKey()
    {
        WarningConfig warningConfig = this.getWarningConfig();
        if (warningConfig == null)
        {
            logger.error("预警规则不能为空");
            return "";
        }

        return warningConfig.getWarningConfigUpKey();
    }
    
    public String getWarningConfigDownKey()
    {
        WarningConfig warningConfig = this.getWarningConfig();
        if (warningConfig == null)
        {
            logger.error("预警规则不能为空");
            return "";
        }

        return warningConfig.getWarningConfigDownKey();
    }
    
    public String getRatiokey()
    {
        return this.getMeasurePointId() + this.getWarningConfigId();
    }
    
    public String getRatiokeyUp()
    {
        return this.getMeasurePointId() + this.getWarningConfigId()+this.getUpValue();
    }
    
    public String getRatiokeyDown()
    {
        return this.getMeasurePointId() + this.getWarningConfigId()+this.getDownValue();
    }

    public Boolean getUp()
    {
        return up;
    }

    public void setUp(Boolean up)
    {
        this.up = up;
    }

    public Boolean getDown()
    {
        return down;
    }

    public void setDown(Boolean down)
    {
        this.down = down;
    }
}
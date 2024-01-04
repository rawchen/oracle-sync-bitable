package com.lundong.sync.entity.feishu;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author shuangquan.chen
 * @date 2023-12-03 10:05
 */
@Data
public class FeishuUser {
    @JSONField(name = "union_id")
    private String unionId;
    @JSONField(name = "user_id")
    private String userId;
    @JSONField(name = "open_id")
    private String openId;
    @JSONField(name = "name")
    private String name;
    @JSONField(name = "en_name")
    private String enName;
    @JSONField(name = "nickname")
    private String nickName;
    @JSONField(name = "email")
    private String email;
    @JSONField(name = "mobile")
    private String mobile;
    @JSONField(name = "mobile_visible")
    private String mobileVisible;
    @JSONField(name = "gender")
    private String gender;
    private String departmentId;
    @JSONField(name = "leader_user_id")
    private String leaderUserId;
    @JSONField(name = "city")
    private String city;
    @JSONField(name = "country")
    private String country;
    @JSONField(name = "work_station")
    private String workStation;
    @JSONField(name = "join_time")
    private String joinTime;
    @JSONField(name = "employeeNo")
    private String employeeNo;
    @JSONField(name = "employee_type")
    private String employeeType;
    @JSONField(name = "enterprise_email")
    private String enterpriseEmail;
    @JSONField(name = "job_title")
    private String jobTitle;
    private String deptName;

    /**
     * 自定义字段
     */
    private String costCenterCode;
    private String companyCode;
}

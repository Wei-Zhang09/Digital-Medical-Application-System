package com.qst.medical.service.impl;

import com.github.pagehelper.PageHelper;
import com.qst.medical.common.PageBean;
import com.qst.medical.common.PageUtil;
import com.qst.medical.common.SortUtil;
import com.qst.medical.domain.dataobject.AccountDo;
import com.qst.medical.domain.entity.Doctor;
import com.qst.medical.domain.entity.DoctorLevel;
import com.qst.medical.domain.entity.TreatType;
import com.qst.medical.domain.param.DoctorParam;
import com.qst.medical.mapper.AccountMapper;
import com.qst.medical.mapper.DoctorLevelMapper;
import com.qst.medical.mapper.DoctorMapper;
import com.qst.medical.mapper.TreatTypeMapper;
import com.qst.medical.service.DoctorService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class DoctorServiceImpl implements DoctorService {

    /** 医师角色的 utype，与 permission/role_permission 里的 ROLE_2 一致 */
    private static final String ROLE_DOCTOR = "ROLE_2";
    /** 新建医师的初始口令 */
    private static final String DEFAULT_PASSWORD = "123456";

    @Resource
    private DoctorMapper doctorMapper;
    @Resource
    private DoctorLevelMapper doctorLevelMapper;
    @Resource
    private TreatTypeMapper treatTypeMapper;
    @Resource
    private AccountMapper accountMapper;
    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public PageBean getDoctorList(Integer pageNum, Integer pageSize, String name,
                                  Integer levelId, Integer typeId, String sort, String order) {
        PageHelper.startPage(pageNum, pageSize, SortUtil.build("doctor", sort, order));
        return PageUtil.toPageBean(doctorMapper.selectDoctor(name, levelId, typeId));
    }

    @Override
    public List<DoctorLevel> getAllLevel() {
        return doctorLevelMapper.selectAll();
    }

    @Override
    public List<TreatType> getAllTreatType() {
        return treatTypeMapper.selectAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addDoctor(DoctorParam param) {
        String uname = resolveUname(param);
        if (accountMapper.countByUname(uname) > 0) {
            throw new IllegalArgumentException("登录名已存在：" + uname);
        }

        Date now = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        AccountDo account = new AccountDo();
        account.setRealname(param.getName().trim());
        account.setUname(uname);
        account.setPwd(passwordEncoder.encode(DEFAULT_PASSWORD));
        account.setPhonenumber(param.getPhone().trim());
        account.setUtype(ROLE_DOCTOR);
        account.setCreatetime(now);
        account.setUpdatetime(now);
        accountMapper.insertAccount(account);

        Doctor doctor = new Doctor();
        doctor.setName(param.getName().trim());
        doctor.setAge(param.getAge());
        doctor.setSex(param.getSex());
        doctor.setLevelId(param.getLevelId());
        doctor.setPhone(param.getPhone().trim());
        doctor.setTypeId(param.getTypeId());
        doctor.setHospital(param.getHospital().trim());
        doctor.setAccountId(account.getId());
        doctor.setCreatetime(LocalDateTime.now());
        doctor.setUpdatetime(LocalDateTime.now());
        doctorMapper.insertDoctor(doctor);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDoctor(Long id, DoctorParam param) {
        Doctor doctor = doctorMapper.selectById(id);
        if (doctor == null) {
            throw new IllegalArgumentException("医师不存在：" + id);
        }
        doctor.setName(param.getName().trim());
        doctor.setAge(param.getAge());
        doctor.setSex(param.getSex());
        doctor.setLevelId(param.getLevelId());
        doctor.setPhone(param.getPhone().trim());
        doctor.setTypeId(param.getTypeId());
        doctor.setHospital(param.getHospital().trim());
        doctor.setUpdatetime(LocalDateTime.now());
        doctorMapper.updateDoctor(doctor);

        // 同步账号上的姓名与手机号
        if (doctor.getAccountId() != null) {
            AccountDo account = accountMapper.selectById(doctor.getAccountId());
            if (account != null) {
                account.setRealname(doctor.getName());
                account.setPhonenumber(doctor.getPhone());
                account.setUpdatetime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
                accountMapper.updateAccountBasic(account);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDoctor(Long id) {
        Doctor doctor = doctorMapper.selectById(id);
        if (doctor == null) {
            throw new IllegalArgumentException("医师不存在：" + id);
        }
        doctorMapper.deleteById(id);
        if (doctor.getAccountId() != null) {
            accountMapper.deleteById(doctor.getAccountId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long id) {
        Doctor doctor = doctorMapper.selectById(id);
        if (doctor == null || doctor.getAccountId() == null) {
            throw new IllegalArgumentException("医师或其账号不存在：" + id);
        }
        accountMapper.updatePwd(doctor.getAccountId(), passwordEncoder.encode(DEFAULT_PASSWORD));
    }

    /**
     * 登录名规则：前端填了就用前端的；否则「姓名 + 手机号后 4 位」，与官方示例（王小明4125）一致。
     */
    private String resolveUname(DoctorParam param) {
        if (param.getUname() != null && !param.getUname().trim().isEmpty()) {
            return param.getUname().trim();
        }
        String phone = param.getPhone() == null ? "" : param.getPhone().trim();
        String suffix = phone.length() >= 4 ? phone.substring(phone.length() - 4) : phone;
        return param.getName().trim() + suffix;
    }
}

package com.qst.medical.mapper;

import com.qst.medical.domain.entity.Doctor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DoctorMapper {

    /** 按姓名模糊查询，联表带出级别名与类型名 */
    List<Doctor> selectDoctor(@Param("name") String name);

    Doctor selectById(@Param("id") Long id);

    int insertDoctor(Doctor doctor);

    int updateDoctor(Doctor doctor);

    int deleteById(@Param("id") Long id);
}

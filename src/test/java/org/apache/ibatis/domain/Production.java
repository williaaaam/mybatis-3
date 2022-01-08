package org.apache.ibatis.domain;

import java.util.Date;

/**
 * @author Williami
 * @description
 * @date 2022/1/8
 */
public class Production {

  Integer id;

  String name;

  Date createTime;

  Date updateTime;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public Date getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }

  @Override
  public String toString() {
    return "Production{" +
      "id=" + id +
      ", name='" + name + '\'' +
      ", createTime=" + createTime +
      ", updateTime=" + updateTime +
      '}';
  }

}

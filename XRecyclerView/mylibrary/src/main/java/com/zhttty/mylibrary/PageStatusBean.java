package com.zhttty.mylibrary;

/**
 */
public class PageStatusBean {
    int pId = 0;//已经获取数据列表最后一条数据的pId 没有传1
    int size = 20;//每页大小 默认为20

    public PageStatusBean() {
    }

    public PageStatusBean(int pId, int size) {
        this.pId = pId;
        this.size = size;
    }

    public int getpId() {
        return pId;
    }

    public void setpId(int pId) {
        this.pId = pId;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "PageStatusBean{" +
                "pId=" + pId +
                ", size=" + size +
                '}';
    }
}

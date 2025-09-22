package com.acf.careerfinder.model;

import jakarta.persistence.*;

@Entity
@Table(name = "q_item",
        indexes = {
                @Index(name = "idx_q_item_section_order", columnList = "section_key, order_index")
        })
public class QItem {

    public enum QType { SINGLE, MULTI, TEXT }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "qkey", nullable = false, length = 64, unique = true)
    private String qkey;

    @Column(name = "section_key", nullable = false, length = 32)
    private String sectionKey;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Enumerated(EnumType.STRING)
    @Column(name = "qtype", nullable = false, length = 16)
    private QType qtype;

    @Column(name = "required")
    private Boolean required = Boolean.TRUE;

    @Column(name = "active")
    private Boolean active = Boolean.TRUE;

    @Column(name = "meta_json", columnDefinition = "text")
    private String metaJson;

    // --- getters/setters ---
    public Long getId() { return id; }
    public String getQkey() { return qkey; }
    public void setQkey(String qkey) { this.qkey = qkey; }

    public String getSectionKey() { return sectionKey; }
    public void setSectionKey(String sectionKey) { this.sectionKey = sectionKey; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

    public QType getQtype() { return qtype; }
    public void setQtype(QType qtype) { this.qtype = qtype; }

    public Boolean getRequired() { return required; }
    public void setRequired(Boolean required) { this.required = required; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public String getMetaJson() { return metaJson; }
    public void setMetaJson(String metaJson) { this.metaJson = metaJson; }
}
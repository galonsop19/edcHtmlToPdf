package com.credix.edcHtmlToPdf.mysqlEDC.entities;

import javax.persistence.*;

@Entity
@Table(name = "credixcrsgc_qa.tb_plantillas_edc")
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plantilla")
    private  String text;
    @Column(name = "cartera")
    private  String cutDate;
    @Column(name = "tipo")
    private  int category;
    @Column(name = "seccion")
    private  int section;
    @Column(name = "estado")
    private  int status;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

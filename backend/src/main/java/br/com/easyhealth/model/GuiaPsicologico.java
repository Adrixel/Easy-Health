package br.com.easyhealth.model;

import java.util.List;

public class GuiaPsicologico {
    private String slug;
    private String titulo;
    private List<String> manterSeguranca;
    private List<String> comoAjudar;

    public GuiaPsicologico(String slug, String titulo, List<String> manterSeguranca, List<String> comoAjudar) {
        this.slug = slug;
        this.titulo = titulo;
        this.manterSeguranca = manterSeguranca;
        this.comoAjudar = comoAjudar;
    }

    public String getSlug() { return slug; }
    public String getTitulo() { return titulo; }
    public List<String> getManterSeguranca() { return manterSeguranca; }
    public List<String> getComoAjudar() { return comoAjudar; }
}

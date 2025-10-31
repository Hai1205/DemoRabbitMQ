package com.example.mailer;

public class UserDto {
    private Long id;
    private String email;
    private String fullName;
    public UserDto() {}
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getEmail(){return email;} public void setEmail(String e){this.email=e;}
    public String getFullName(){return fullName;} public void setFullName(String f){this.fullName=f;}
}

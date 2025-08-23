package com.example.emergency_department_tracking_system;



public class registrations {
    public String usernameString;
    public String passwordString;
    public String nameString;


    public String mobileNumberString;

    public String emergencyMobileNumber2String;

    public String gender;
    public String date;

    public registrations() {
    }

    public registrations(String usernameString, String passwordString, String nameString,
                         String mobileNumberString,
                         String emergencyMobileNumber2String, String gender,  String date) {
        this.usernameString = usernameString;
        this.passwordString = passwordString;
        this.nameString = nameString;


        this.mobileNumberString = mobileNumberString;

        this.emergencyMobileNumber2String = emergencyMobileNumber2String;

        this.gender = gender;

        this.date = date;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }





    public String getUsernameString() {
        return usernameString;
    }

    public void setUsernameString(String usernameString) {
        this.usernameString = usernameString;
    }

    public String getPasswordString() {
        return passwordString;
    }

    public void setPasswordString(String passwordString) {
        this.passwordString = passwordString;
    }

    public String getNameString() {
        return nameString;
    }

    public void setNameString(String nameString) {
        this.nameString = nameString;
    }




    public String getMobileNumberString() {
        return mobileNumberString;
    }

    public void setMobileNumberString(String mobileNumberString) {
        this.mobileNumberString = mobileNumberString;
    }






    public String getEmergencyMobileNumber2String() {
        return emergencyMobileNumber2String;
    }

    public void setEmergencyMobileNumber2String(String emergencyMobileNumber2String) {
        this.emergencyMobileNumber2String = emergencyMobileNumber2String;
    }



    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

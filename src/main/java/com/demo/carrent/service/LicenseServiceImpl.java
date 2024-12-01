package com.demo.carrent.service;

import com.demo.carrent.dto.LicenseDto;
import com.demo.carrent.dto.response.DeleteResponse;
import com.demo.carrent.entity.License;
import com.demo.carrent.entity.User;
import com.demo.carrent.repository.LicenseRepository;
import com.demo.carrent.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class LicenseServiceImpl implements LicenseService{

    private final LicenseRepository licenseRepository;

    private final UserRepository userRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    public LicenseServiceImpl(LicenseRepository licenseRepository,UserRepository userRepository){
        this.licenseRepository=licenseRepository;
        this.userRepository=userRepository;
    }

    @Override
    public License addLicenseDetails(MultipartFile file, LicenseDto licenseDto) throws IOException{

        //find the user
        User user=userRepository.findById(licenseDto.getUserId()).orElse(null);

        if(user!=null){
            License license=new License();

            license.setLicenseNumber(licenseDto.getLicenseNumber());
            license.setExpirationDate(licenseDto.getExpirationDate());

            //set image and image location
            license.setImagePath(uploadFile(file, licenseDto.getLicenseNumber()));
            license.setUser(user);

            licenseRepository.save(license);

            return license;
        }else{
            return null;
        }

    }

    @Override
    public License getLicenseById(Long id) {
        return null;
    }

    @Override
    public List<License> getAllLicenseDetails() {
        return List.of();
    }

    @Override
    public DeleteResponse deleteLicenseDetails(Long id) {
        return null;
    }

    //uploading file
    public String uploadFile(MultipartFile file,String licenseNumber)throws IOException {
        //Ensure the directory exists
        File dir=new File(uploadDir);
        if(!dir.exists()){
            dir.mkdir();
        }

        //define file name
        String fileName="user_"+licenseNumber;
        Path path= Paths.get(uploadDir,fileName);

        //save the file to the specified directory
        Files.write(path,file.getBytes());

        //return the path for verification
        return path.toString();
    }

}

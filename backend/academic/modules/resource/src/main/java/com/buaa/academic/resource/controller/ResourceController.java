package com.buaa.academic.resource.controller;

import com.buaa.academic.model.exception.AcademicException;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.text.RandomStringGenerator;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.apache.commons.text.CharacterPredicates.DIGITS;
import static org.apache.commons.text.CharacterPredicates.LETTERS;

@RestController
@Validated
@Api(tags = "静态资源管理")
public class ResourceController {

    private static final RandomStringGenerator generator = new RandomStringGenerator.Builder()
            .withinRange('0', 'Z')
            .filteredBy(LETTERS, DIGITS)
            .build();

    @Value("${resource.storage-directory}")
    private String directory;

    @Value("${resource.allowed-suffixes}")
    private String allowedSuffixes;

    private String[] suffixes;

    @PostConstruct
    public void init() {
        this.suffixes = allowedSuffixes.strip().split("[,;:/\\s]+");
        for (int i = 0; i < suffixes.length; ++i) {
            suffixes[i] = suffixes[i].toLowerCase();
        }
    }

    @PostMapping("/upload")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "文件token，若不传代表上传新文件，否则替换之前上传的文件"),
            @ApiImplicitParam(name = "file", value = "上传的文件")})
    @ApiOperation(value = "上传文件", notes = "上传一个20MB以内的文件，并返回其对应的token")
    public Result<String> upload(@RequestParam(name = "token", required = false) @Length(min = 64, max = 64) @Pattern(regexp = "[0-9A-Z]+") String token,
                                 @RequestParam(name = "file") MultipartFile multipartFile) throws IOException {
        Result<String> result = new Result<>();
        if (token == null)
            token = generator.generate(64);
        File folder = new File(directory, token).getAbsoluteFile();
        if (folder.exists()) {
            recursiveDelete(folder);
        }
        if (!folder.mkdirs()) {
            throw new IOException("Cannot create folder");
        }
        String filename = multipartFile.getOriginalFilename();
        if (!validateSuffix(filename))
            return result.withFailure(ExceptionType.INVALID_PARAM);
        File file = new File(folder, filename);
        multipartFile.transferTo(file);
        return new Result<String>().withData(token);
    }

    @GetMapping("/download")
    @ApiOperation(value = "下载文件", notes = "根据传入的token下载对应的文件")
    @ApiImplicitParam(name = "token", value = "文件token")
    public void download(@RequestParam(name = "token") @NotEmpty @Length(min = 64, max = 64) String token,
                         HttpServletResponse response) throws AcademicException, IOException {
        File folder = new File(directory, token).getAbsoluteFile();
        if (!folder.exists() || !folder.isDirectory()) {
            throw new AcademicException(ExceptionType.NOT_FOUND);
        }
        File[] files = folder.listFiles();
        if (files == null) {
            throw new IOException("unexpected null old file list");
        }
        if (files.length == 0)
            throw new IOException("folder is empty");
        File file = files[0];
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            OutputStream os = response.getOutputStream();
            String fileName = file.getName();
            response.setContentType("application/force-download");
            response.addHeader("Content-Disposition","attachment; filename="
                    + URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20"));
            byte[] buffer = new byte[1024];
            int i = bis.read(buffer);
            while (i != -1) {
                os.write(buffer, 0, i);
                i = bis.read(buffer);
            }
        }
    }

    private void recursiveDelete(File file) throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null)
                throw new IOException("unexpected null old file list");
            for (File child : files) {
                recursiveDelete(child);
            }
        }
        if (!file.delete()) {
            throw new IOException("cannot delete old file");
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean validateSuffix(String filename) {
        if (filename == null)
            return false;
        String suffix = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        for (String s : suffixes) {
            if (s.equals(suffix))
                return true;
        }
        return false;
    }

}

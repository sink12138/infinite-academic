package com.buaa.academic.resource.controller;

import com.buaa.academic.model.exception.AcademicException;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import com.buaa.academic.resource.service.TokenService;
import com.buaa.academic.resource.util.FolderUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
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

    @Autowired
    private TokenService tokenService;

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
    @ApiOperation(value = "上传文件", notes = "上传一个20MB以内的文件，并返回其对应的token")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "文件token，若不传代表上传新文件，否则替换之前上传的文件"),
            @ApiImplicitParam(name = "file", value = "上传的文件")})
    public Result<String> upload(@RequestParam(name = "token", required = false) @Pattern(regexp = "^[0-9A-Z]{64}$") String token,
                                 @RequestParam(name = "file") MultipartFile multipartFile) throws IOException {
        Result<String> result = new Result<>();
        if (token == null)
            token = generator.generate(64);
        else if (!tokenService.existsToken(token))
            return result.withFailure(ExceptionType.NOT_FOUND);
        File folder = new File(directory, token).getAbsoluteFile();
        if (folder.exists()) {
            FolderUtils.removeFolder(folder);
        }
        if (!folder.mkdirs()) {
            throw new IOException("Cannot create folder");
        }
        String filename = multipartFile.getOriginalFilename();
        if (!validateSuffix(filename))
            return result.withFailure("不支持的文件类型");
        File file = new File(folder, filename);
        multipartFile.transferTo(file);
        tokenService.addToken(token);
        return new Result<String>().withData(token);
    }

    @GetMapping("/download")
    @ApiOperation(value = "下载文件", notes = "根据传入的token下载对应的文件")
    @ApiImplicitParam(name = "token", value = "文件token")
    public void download(@RequestParam(name = "token") @Pattern(regexp = "^[0-9A-Z]{64}$") String token,
                         HttpServletResponse response) throws AcademicException, IOException {
        File folder = new File(directory, token).getAbsoluteFile();
        if (!folder.exists() || !folder.isDirectory()) {
            throw new AcademicException(ExceptionType.NOT_FOUND);
        }
        File[] files = folder.listFiles();
        if (files == null)
            throw new IOException("unexpected null old file list");
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
            bis.close();
            fis.close();
            os.flush();
            os.close();
        }
    }

    @PostMapping("/remove")
    @ApiOperation(value = "删除文件", notes = "根据传入的token删除对应的文件")
    @ApiImplicitParam(name = "token", value = "文件token")
    public Result<Void> remove(@RequestParam(name = "token") @Pattern(regexp = "^[0-9A-Z]{64}$") String token)
            throws AcademicException, IOException {
        if (!tokenService.existsToken(token))
            throw new AcademicException(ExceptionType.NOT_FOUND);
        FolderUtils.removeFolder(new File(directory, token).getAbsoluteFile());
        return new Result<>();
    }

    @GetMapping("/exists")
    @ApiOperation(value = "资源是否存在", notes = "判断传入的token是否存在对应的文件资源")
    @ApiImplicitParam(name = "token", value = "文件token")
    public Result<Boolean> exists(@RequestParam(name = "token") @Pattern(regexp = "^[0-9A-Z]{64}$") String token) {
        return new Result<Boolean>().withData(tokenService.existsToken(token));
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

package com.gytni.licenseclassify.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.gytni.licenseclassify.Type.LicenseType;
import com.gytni.licenseclassify.model.CSVUploadPattern;

import fasttext.FastText;
import fasttext.FastTextPrediction;
import jakarta.annotation.PostConstruct;

@Service
public class FastTextProbabilityService {
    private FastText productModel;
    private FastText publisherModel;

    private String productModelPath = "C:\\DEV\\fasttext_models\\0110_appwiz_model.fasttext";
    private String publisherModelPath = "C:\\DEV\\fasttext_models\\0110_publisher_model_2.fasttext";

    @PostConstruct
    public void init() throws Exception {
        productModel = FastText.loadModel(productModelPath);
        publisherModel = FastText.loadModel(publisherModelPath);
    }
    
    public String probability(CSVUploadPattern productPattern) {
        List<FastTextPrediction> ps = productModel.predictAll(productPattern.getProductName());
        List<FastTextPrediction> pbs = null;

        if (StringUtils.isNotBlank(productPattern.getPublisher())) 
            pbs = publisherModel.predictAll(productPattern.getPublisher());

        FastTextPrediction p = ps.get(0);
        FastTextPrediction pb = null;

        if (pbs != null)
            pb = pbs.get(0);
        
        double pbb = p.probability();
        double bbb = 0.0;

        if (pb != null)
            bbb = pb.probability();

        return pbb > bbb ? transformationType(p.label()) : (pb == null ? LicenseType.NONE.name() : transformationType(pb.label()));
    }

    private String transformationType(String result) {
        if (result == null || result.isEmpty()) return LicenseType.NONE.name();

        if (result.equals("__label__상용"))
            return LicenseType.COMMERCIAL.name();
        else if (result.equals("__label__프리"))
            return LicenseType.FREE.name();
        else if (result.equals("__label__쉐어"))
            return LicenseType.SHAREWARE.name();
        else if (result.equals("__label__기타"))
            return LicenseType.ETC.name();

        return LicenseType.NONE.name();
    }

}

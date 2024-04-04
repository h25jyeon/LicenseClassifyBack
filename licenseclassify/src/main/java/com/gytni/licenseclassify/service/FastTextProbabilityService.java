package com.gytni.licenseclassify.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.gytni.licenseclassify.Type.LicenseType;

import fasttext.FastText;
import fasttext.FastTextPrediction;
import jakarta.annotation.PostConstruct;

@Service
public class FastTextProbabilityService {
    private FastText productModel;
    private FastText publisherModel;

    // @Value("${app.ai.product.path}")
    private String productModelPath = "C:\\DEV\\fasttext_models\\0110_appwiz_model.fasttext";

    // @Value("${app.ai.publisher.path}")
    private String publisherModelPath = "C:\\DEV\\fasttext_models\\0110_publisher_model_2.fasttext";

    @PostConstruct
    public void init() throws Exception {
        productModel = FastText.loadModel(productModelPath);
        publisherModel = FastText.loadModel(publisherModelPath);
    }

    public String probability(String appwizName, String publisher) {
        List<FastTextPrediction> ps = productModel.predictAll(appwizName);
        List<FastTextPrediction> pbs = null;

        if (StringUtils.isNotBlank(publisher)) 
            pbs = publisherModel.predictAll(publisher);

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

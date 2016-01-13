package se.inera.intyg.webcert.integration.pp.stub;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import se.riv.infrastructure.directory.privatepractitioner.v1.HoSPersonType;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Magnus Ekstrand on 18/06/15.
 */
public class PPBootstrapBean {

    private static final Logger LOG = LoggerFactory.getLogger(PPBootstrapBean.class);

    @Autowired
    private HoSPersonStub personStub;

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void bootstrapPrivatePractitioners() throws IOException {
        List<Resource> files = getResourceListing("bootstrap-privatepractitioner/*.json");
        LOG.debug("Bootstrapping {} private practitioners for PP stub ...", files.size());
        for (Resource res : files) {
            try {
                add(res);
            } catch (Exception e) {
                LOG.error("Failure while bootstrapping private practitioners", e);
            }
        }
    }

    private List<Resource> getResourceListing(String classpathResourcePath) {
        try {
            PathMatchingResourcePatternResolver r = new PathMatchingResourcePatternResolver();
            return Arrays.asList(r.getResources(classpathResourcePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void add(Resource res) throws IOException {
        LOG.debug("Loading private practitioners from " + res.getFilename());
        HoSPersonType personType = objectMapper.readValue(res.getInputStream(), HoSPersonType.class);
        personStub.add(personType);
        LOG.debug(String.format("Loaded private practitioner %s (%s)", personType.getFullstandigtNamn(), personType.getPersonId().getExtension()));
    }

}

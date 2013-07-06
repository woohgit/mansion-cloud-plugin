package com.cloudbees.jenkins.plugins.mtslavescloud;

import com.cloudbees.mtslaves.client.VirtualMachineRef;
import com.cloudbees.mtslaves.client.VirtualMachineSpec;
import hudson.util.IOUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.apache.commons.io.IOUtils.*;

/**
 * Configuration of multi-tenant slave, described in a format that {@link VirtualMachineSpec} understands.
 *
 * <p>
 * This is how people can craft, share, and reuse the slave definition, and therefore there is no
 * details specific to each Jenkins installations (such as which label this slave is available as.)
 *
 * @author Kohsuke Kawaguchi
 */
public class SlaveTemplate {
    /**
     * Unique ID that globally distinguishes this template.
     */
    public String id;
    /**
     * Human readable name of this template.
     */
    public String displayName;
    /**
     * Configuration JSON object to be passed to {@link VirtualMachineRef#setup(VirtualMachineSpec)}
     */
    public JSONObject spec;

    public void populate(VirtualMachineSpec spec) {
        JSONArray configs = this.spec.optJSONArray("configs");
        if (configs!=null) {
            spec.configs.addAll(configs);
        }
    }

    public static Map<String,SlaveTemplate> load(InputStream in) throws IOException {
        try {
            JsonConfig jsc = new JsonConfig();
            jsc.setIgnorePublicFields(false);
            JSONObject js = JSONObject.fromObject(IOUtils.toString(in));
            TemplateList list = (TemplateList) JSONObject.toBean(js, TemplateList.class);
            Map<String,SlaveTemplate> r = new LinkedHashMap<String, SlaveTemplate>();
            for (SlaveTemplate t : list.templates) {
                r.put(t.id,t);
            }
            return r;
        } finally {
            closeQuietly(in);
        }
    }

    public static final class TemplateList {
        public SlaveTemplate[] templates;
    }
}
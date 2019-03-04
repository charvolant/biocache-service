/**************************************************************************
 *  Copyright (C) 2010 Atlas of Living Australia
 *  All Rights Reserved.
 *
 *  The contents of this file are subject to the Mozilla Public
 *  License Version 1.1 (the "License"); you may not use this file
 *  except in compliance with the License. You may obtain a copy of
 *  the License at http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS
 *  IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  rights and limitations under the License.
 ***************************************************************************/
package au.org.ala.biocache.service;

import au.org.ala.biocache.dao.SearchDAO;
import au.org.ala.biocache.dao.SearchDAOImpl;
import au.org.ala.biocache.dto.SpeciesImageDTO;
import au.org.ala.biocache.dto.SpeciesImagesDTO;
import au.org.ala.biocache.util.SearchUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;

/**
 * cache of lft with the first found image info; data_resource_uid, image_url and number found.
 *
 * This does not wait for the cache to be built.
 *
 * Created by Adam Collins on 21/09/15.
 */
@Component("SpeciesImageService")
public class SpeciesImageService {

    /** log4 j logger */
    private static final Logger logger = Logger.getLogger(SpeciesImageService.class);

    /**
     * Fulltext search DAO
     */
    @Inject
    protected SearchDAO searchDAO;

    private Object cacheLock = new Object();
    private SpeciesImagesDTO cache = RestartDataService.get(this, "cache", new TypeReference<SpeciesImagesDTO>(){}, SpeciesImagesDTO.class);
    private boolean updatingCache = false;

    Thread updateCacheThread = new CacheThread();

    class CacheThread extends Thread {
        @Override
        public void run() {
            try {
                long startTime = System.currentTimeMillis();
                logger.debug("start refresh");

                //lft counts and one data_resource_uid and one image_url for each
                SolrQuery query = new SolrQuery();
                query.setFacet(false);
                query.setRows(0);

                // count unique pointTypes
                query.add("json.facet", "{lft: {type: terms, limit: -1, field: lft, sort: index, " +
                        "facet:{" +
                        "dr: {type: terms, limit: 1, field: data_resource_uid, sort: index}, " +
                        "img:{type: terms, limit: 1, field: image_url, sort: index}}}}");
                QueryResponse qr = ((SearchDAOImpl) ((Advised) searchDAO).getTargetSource().getTarget()).query(query, null);

                Map<Long, SpeciesImageDTO> map = new HashMap();

                for (SimpleOrderedMap item : SearchUtils.getList(qr.getResponse(), "facets", "lft", "buckets")) {
                    String dataResourceUid = (String) SearchUtils.getVal(item, "dr", "buckets", 0, 0);
                    String imageUrl = (String) SearchUtils.getVal(item, "dr", "buckets", 0, 0);
                    SpeciesImageDTO image = new SpeciesImageDTO(dataResourceUid, imageUrl);
                    image.setCount((Long) item.getVal(1));
                    try {
                        map.put((Long) item.getVal(0), image);
                    } catch (Exception e) {
                    }
                }

                //sort keys
                long[] left = new long[map.size()];
                List<Long> keys = new ArrayList<Long>(map.keySet());
                for (int i = 0; i < left.length; i++) {
                    left[i] = keys.get(i);
                }
                Arrays.sort(left);

                //get sorted values
                SpeciesImageDTO[] leftImages = new SpeciesImageDTO[map.size()];
                for (int i = 0; i < leftImages.length; i++) {
                    leftImages[i] = map.get(left[i]);
                }

                SpeciesImagesDTO speciesImages = new SpeciesImagesDTO(left, leftImages);

                //store in map
                synchronized (cacheLock) {
                    updatingCache = false;
                    if (speciesImages.getSpeciesImage().length > 0) {
                        cache = speciesImages;
                    }
                }

                logger.debug("time to refresh SpeciesImageService: " + (System.currentTimeMillis() - startTime) + "ms");
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @PostConstruct
    public void init() {
        resetCache();
    }


    /**
     * Permit disabling of cached species images
     */
    @Value("${autocomplete.species.images.enabled:true}")
    private Boolean enabled;

    /**
     * retrieve left + count + index version
     *
     * @return
     */
    public SpeciesImagesDTO getSpeciesImages() {
        if (!enabled) return null;

        if (cache.getSpeciesImage() == null) {
            synchronized (cacheLock) {
                if (!updatingCache && cache == null) {
                    updatingCache = true;
                    updateCacheThread.start();
                }
            }
        }
        return cache;
    }

    public SpeciesImageDTO get(long left, long right) {
        SpeciesImagesDTO speciesImages = getSpeciesImages();
        if (speciesImages == null || speciesImages.getLft() == null) {
            return null;
        }
        long[] lft = speciesImages.getLft();
        SpeciesImageDTO[] images = speciesImages.getSpeciesImage();

        int pos = java.util.Arrays.binarySearch(lft, left);
        if (pos < 0) {
            pos = -1 * pos - 1;
        }

        //get the first image
        SpeciesImageDTO ret = null;
        if (pos < lft.length && lft[pos] < right) {
            ret = new SpeciesImageDTO(images[pos].getDataResourceUid(), images[pos].getImage());
        }

        long sum = 0;
        while (pos < lft.length && lft[pos] < right) {
            sum += images[pos++].getCount();
        }

        if (ret != null && sum > 0) {
            ret.setCount(sum);
        }

        return ret;
    }

    public void resetCache() {
        synchronized (cacheLock) {
            if (!updatingCache) {
                updatingCache = true;
                updateCacheThread = new CacheThread();
                updateCacheThread.start();
            }
        }
    }

}

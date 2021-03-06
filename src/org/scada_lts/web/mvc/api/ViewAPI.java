/*
 * (c) 2017 Abil'I.T. http://abilit.eu/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.scada_lts.web.mvc.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serotonin.mango.Common;
import com.serotonin.mango.view.View;
import com.serotonin.mango.vo.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.scada_lts.dao.model.view.ViewDTO;
import org.scada_lts.mango.service.ViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Arkadiusz Parafiniuk arkadiusz.parafiniuk@gmail.com
 */
@Controller
public class ViewAPI {


    private static final Log LOG = LogFactory.getLog(WatchListAPI.class);
    private static final int ID_USER_AMIN = 1;

    ViewService viewService = new ViewService();

    @RequestMapping(value = "/api/view/getAll", method = RequestMethod.GET)
    public ResponseEntity<String> getAll(HttpServletRequest request) {
        LOG.info("/api/view/getAll");

        try {
            User user = Common.getUser(request);

            if (user != null) {
                class ViewJSON implements Serializable {
                    private long id;
                    private String xid;

                    ViewJSON(long id, String xid) {
                        this.setId(id);
                        this.setXid(xid);
                    }

                    public long getId() {
                        return id;
                    }

                    public void setId(long id) {
                        this.id = id;
                    }

                    public String getXid() {
                        return xid;
                    }

                    public void setXid(String xid) {
                        this.xid = xid;
                    }
                }

                int userId = user.getId();
                List<View> lstV;
                if (userId == ID_USER_AMIN) {
                    lstV = viewService.getViews();
                } else {
                    return new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);
                }

                List<ViewJSON> lst = new ArrayList<>();
                for (View view : lstV) {
                    ViewJSON vJ = new ViewJSON(view.getId(), view.getXid());
                    lst.add(vJ);
                }

                String json = null;
                ObjectMapper mapper = new ObjectMapper();
                json = mapper.writeValueAsString(lst);

                return new ResponseEntity<String>(json, HttpStatus.OK);
            }

            return new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            LOG.error(e);
            return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/api/view/createView", method = RequestMethod.POST)
    public ResponseEntity<String> createView(HttpServletRequest request, @RequestBody ViewDTO viewDTO) {
        LOG.info("/api/view/createView");

        ResponseEntity<String> result;

        try {
            class ViewJSON implements Serializable {
                private static final long serialVersionUID = 8076556272526329317L;
                private String name;
                private String xid;
                private int userId;
                private int resolution;
                private String filename;

                public ViewJSON(String name, String xid, int userId, int resolution, String filename) {
                    this.name = name;
                    this.xid = xid;
                    this.userId = userId;
                    this.resolution = resolution;
                    this.filename = filename;
                }

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public String getXid() {
                    return xid;
                }

                public void setXid(String xid) {
                    this.xid = xid;
                }

                public int getUserId() {
                    return userId;
                }

                public void setUserId(int userId) {
                    this.userId = userId;
                }

                public int getResolution() {
                    return resolution;
                }

                public void setResolution(int resolution) {
                    this.resolution = resolution;
                }

                public String getFilename() {
                    return filename;
                }

                public void setFilename(String filename) {
                    this.filename = filename;
                }
            }

            User user = Common.getUser(request);

            if (user.isAdmin()) {

                View view = new View();
                view.setName(viewDTO.getName());
                view.setXid(viewDTO.getXid());
                view.setResolution(viewDTO.getSize());
                view.setBackgroundFilename(viewDTO.getImagePath());
                view.setUserId(user.getId());

                viewService.saveView(view);

                ObjectMapper mapper = new ObjectMapper();
                String json = null;
                ViewJSON viewJSON = new ViewJSON(
                        view.getName(),
                        view.getXid(),
                        view.getUserId(),
                        view.getResolution(),
                        view.getBackgroundFilename()
                );

                json = mapper.writeValueAsString(viewJSON);

                result = new ResponseEntity<String>(json, HttpStatus.OK);
            } else {
                result = new ResponseEntity<String>(HttpStatus.UNAUTHORIZED);
            }

        } catch (Exception e) {
            LOG.error(e);
            result = new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
        }
        return result;

    }

}

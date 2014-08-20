/*
*  Copyright 2014 TWO SIGMA OPEN SOURCE, LLC
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*         http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/

(function() {
  'use strict';
  var retfunc = function(plotUtils) {
    var PlotAuxRiver = function(data){
      _(this).extend(data); // copy properties to itself
      this.format();
    };

    PlotAuxRiver.prototype.plotClass = "";
    PlotAuxRiver.prototype.respClass = "plot-resp";

    PlotAuxRiver.prototype.format = function() {
      if (this.color != null) {
        this.tip_color = plotUtils.createColor(this.color, this.color_opacity);
      } else {
        this.tip_color = "gray";
      }
      this.itemProps = {
        "id" : this.id,
        "fi" : this.color,
        "fi_op": this.color_opacity,
        "st": this.stroke,
        "st_w": this.stroke_width,
        "st_op": this.stroke_opacity
      };
      this.elementProps = [];
      this.widthShrink = 0;
    };

    PlotAuxRiver.prototype.setWidthShrink = function(shrink) {
      this.widthShrink = shrink;
    };

    PlotAuxRiver.prototype.render = function(scope, elements, gid){
      this.elements = elements;
      this.prepare(scope, gid);
      this.draw(scope, gid);
    };

    PlotAuxRiver.prototype.prepare = function(scope, gid) {
      var focus = scope.focus;
      var eles = this.elements,
          eleprops = this.elementProps;
      var mapX = scope.data2scrX,
          mapY = scope.data2scrY;
      var pstr = "";

      eleprops.length = 0;

      var eles = this.elements;
      for (var i = 0; i < eles.length; i++) {
        var ele = eles[i];
        var x = mapX(ele.x), y = mapY(ele.min), y2 = mapY(ele.max);

        if (plotUtils.rangeAssert([x, y, y2])) {
          eleprops.length = 0;
          return;
        }

        pstr += x + "," + y + " ";

        if (ele.y <= focus.yr && ele.y2 >= focus.yl) {
          var id = this.id + "_" + i;
          var prop = {
            "id" : id,
            "idx" : this.index,
            "ele" : ele,
            "w" : this.respw,
            "x" : x - this.respw / 2,
            "y" : y2,
            "h" : Math.max(y - y2, this.respminh),  // min height to be hoverable
            "t_x" : x,
            "t_y" : (y + y2) / 2,
            "op" : scope.tips[id] == null ? 0 : 1
          };
          eleprops.push(prop);
        }
      }

      for (var i = eles.length - 1; i >= 0; i--) {
        var ele = eles[i];
        var x = mapX(ele.x), y2 = mapY(ele.max);
        pstr += x + "," + y2 + " ";
      }
      if (pstr.length > 0) {
        this.itemProps.pts = pstr;
      }
    };

    PlotAuxRiver.prototype.draw = function(scope, gid) {
      var svg = scope.maing;
      var props = this.itemProps,
          eleprops = this.elementProps;

      if (svg.select("#" + this.id).empty()) {
        svg.selectAll("g")
          .data([props], function(d) { return d.id; }).enter().append("g")
          .attr("id", function(d) { return d.id; });
      }

      var groupid = this.id + "_" + gid;
      var itemsvg = svg.select("#" + this.id);

      if (itemsvg.select("#" + groupid).empty()) {
        // aux box are ploted as bars with normal coloring
        // if special coloring is needed, it is set from the loader
        itemsvg.selectAll("#" + groupid)
          .data([props]).enter().append("g")
          .attr("id", groupid)
          .attr("class", this.plotClass)
          .style("fill", function(d) { return d.fi; })
          .style("fill-opacity", function(d) { return d.fi_op; })
          .style("stroke", function(d) { return d.st; })
          .style("stroke-opacity", function(d) { return d.st_op; })
          .style("stroke-width", function(d) { return d.st_w; });
      }

      var groupsvg = itemsvg.select("#" + groupid);

      groupsvg.selectAll("polygon")
        .data([props]).enter().append("polygon");
      groupsvg.selectAll("polygon")
        .attr("points", props.pts);

      if (scope.stdmodel.useToolTip === true) {
        itemsvg.selectAll("rect")
          .data(eleprops, function(d) { return d.id; }).exit().remove();
        itemsvg.selectAll("rect")
          .data(eleprops, function(d) { return d.id; }).enter().append("rect")
          .attr("id", function(d) { return d.id; })
          .attr("class", this.respClass)
          .attr("width", function(d) { return d.w; })
          .style("stroke", this.tip_color);

        itemsvg.selectAll("rect")
          .data(eleprops, function(d) { return d.id; })
          .attr("x", function(d) { return d.x; })
          .attr("y", function(d) { return d.y; })
          .attr("height", function(d) { return d.h; })
          .style("opacity", function(d) { return d.op; });
      }
    };

    PlotAuxRiver.prototype.clearTips = function(scope) {
      var eleprops = this.elementProps;
      for (var i = 0; i < eleprops.length; i++) {
        var sel = scope.jqcontainer.find("#tip_" + eleprops[i].id).remove();
        delete scope.tips[eleprops[i].id];  // must clear from tip drawing queue
      }
    };

    return PlotAuxRiver;
  };
  beaker.bkoFactory('PlotAuxRiver', ['plotUtils', retfunc]);
})();

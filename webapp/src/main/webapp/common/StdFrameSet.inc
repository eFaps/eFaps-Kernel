<%--
 
  Copyright 2003-2007 The eFaps Team
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
       http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 
  Author:          tmo
  Revision:        $Rev$
  Last Changed:    $Date$
  Last Changed By: $Author$
 
--%>

<html>
  <head>
  </head>
  <link rel="StyleSheet" href="../styles/eFapsDefault.css" type="text/css"/>
  <script type="text/javascript" src="../javascripts/eFapsDefault.js"></script>
  <body onLoad="eFapsPositionMain()" onUnLoad="eFapsCleanUpSession('<%=timeStamp%>')" class="<%=bodyClass%>">
    <div class="eFapsFrameHeader">
      <div id="eFapsFrameHeaderTitle" class="eFapsFrameHeaderTitle">
        <span id="eFapsFrameHeaderText" class="eFapsFrameHeaderTitle">Loading...</span>
        <img id="eFapsFrameHeaderProgressBar" src="../images/eFapsProcess.gif"/>
      </div>
    </div>
    <iframe frameborder="no" id="eFapsFrameMain" class="eFapsFrameMain" name="genMain" src="<%=urlMain%>">
    </iframe>
    <iframe name="eFapsFrameHidden" id="eFapsFrameHidden" class="eFapsFrameHidden" src="Blank.jsp">
    </iframe>
    <div class="eFapsFrameFooter">
  <!--  sendFSFooter(_context); -->
    </div>
  </body>
</html>

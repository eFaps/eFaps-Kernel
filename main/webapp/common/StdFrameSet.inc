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
    <iframe name="eFapsFrameHidden" id="eFapsFrameHidden" class="eFapsFrameHidden" src="about:blank">
    </iframe>
    <div class="eFapsFrameFooter">
  <!--  sendFSFooter(_context); -->
    </div>
  </body>
</html>

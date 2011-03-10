
<s:url id="en">
	<s:param name="request_locale">en</s:param>
</s:url>
<s:url id="fr">
	<s:param name="request_locale">fr</s:param>
</s:url>

<script language="javascript">

	var currentTab;

	function init() {
		setClientSize();
	}


	function setClientSize() {
		var myTable = document.getElementById('myTable');
		var testLargeur = (document.documentElement && document.documentElement.clientWidth) || window.innerWidth || self.innerWidth || document.body.clientWidth;
		if(testLargeur > 1027) {
			myTable.style.width = (testLargeur - 15);
		} else {
			myTable.style.width = 1027;
		}
		
	}

	function showTab(name) {
		hideTab();
		if(document.getElementById(name) != null)
			document.getElementById(name).style.visibility = "visible";
		currentTab = name;
	}

	function hideTab() {
		if(currentTab != null)
			document.getElementById(currentTab).style.visibility = "hidden";
	}	

	document.onclick = hideTab;

</script>

<div style="position:relative;height:25">
	<DIV id="topdeck" class="popper">
	</DIV>

	<DIV ID=topgauche>
		<TABLE id="myTable" BORDER=0 CELLPADDING=0 CELLSPACING=0 BGCOLOR=#FFFFFF>
			<TR>
				<TD>
					<TABLE CELLPADDING=0 CELLSPACING=1 BORDER=0 WIDTH=100% HEIGHT=35>
						<TR>
							<TD WIDTH=100 ALIGN=center BGCOLOR="#000000" onMouseOver="this.style.background='#7B7B7B';showTab('tab1')" onMouseOut="this.style.background='#000000'">
								<A onClick="return(false)" onMouseOver="showTab('tab1')" href=# CLASS=ejsmenu>
										<s:text name="menu.midlet.head"/>
								</A>
							</TD>
							<TD WIDTH=100 ALIGN=center BGCOLOR="#000000" onMouseOver="this.style.background='#7B7B7B';showTab('tab2')" onMouseOut="this.style.background='#000000'">
								<A onClick="return(false)" onMouseOver="showTab('tab2')" href=# CLASS=ejsmenu>
										<s:text name="menu.mobile.head"/>
								</A>
							</TD>
							<TD WIDTH=100 ALIGN=center BGCOLOR="#000000" onMouseOver="this.style.background='#7B7B7B';showTab('tab3')" onMouseOut="this.style.background='#000000'">
								<A onClick="return(false)" onMouseOver="showTab('tab3')" href=# CLASS=ejsmenu>
										<s:text name="menu.branch.head"/>
								</A>
							</TD>
							<TD WIDTH=100 ALIGN=center BGCOLOR="#000000" onMouseOver="this.style.background='#7B7B7B';showTab('tab4')" onMouseOut="this.style.background='#000000'">
								<A onClick="return(false)" onMouseOver="showTab('tab4')" href=# CLASS=ejsmenu>
										<s:text name="menu.module.head"/>
								</A>
							</TD>							
							<TD WIDTH=100 ALIGN=center BGCOLOR="#000000" onMouseOver="this.style.background='#7B7B7B';showTab('tab5')" onMouseOut="this.style.background='#000000'">
								<A onClick="return(false)" onMouseOver="showTab('tab5')" href=# CLASS=ejsmenu>
										<s:text name="menu.security.head"/>
								</A>
							</TD>
							<TD WIDTH=100 ALIGN=center BGCOLOR="#000000" onMouseOver="this.style.background='#7B7B7B';showTab('tab6')" onMouseOut="this.style.background='#000000'">
								<A onClick="return(false)" onMouseOver="showTab('tab6')" href=# CLASS=ejsmenu>
										<s:text name="menu.service.head"/>
								</A>
							</TD>
							<TD WIDTH=100 ALIGN=center BGCOLOR="#000000" onMouseOver="this.style.background='#7B7B7B';showTab('tab7')" onMouseOut="this.style.background='#000000'">
								<A onClick="return(false)" onMouseOver="showTab('tab7')" href=# CLASS=ejsmenu>
										<s:text name="menu.tools.head"/>
								</A>
							</TD>
							<TD WIDTH=100 ALIGN=center BGCOLOR="#000000" onMouseOver="this.style.background='#7B7B7B';hideTab()" onMouseOut="this.style.background='#000000'">
								<A HREF="session.action" CLASS=ejsmenu>
										<s:text name="menu.home"/>
								</A>
							</TD>
							<TD ALIGN=right BGCOLOR="#000000" onMouseOver="hideTab()">
								<s:a href="%{fr}"><img src="../img/french.gif" border="0" alt="fr"/></s:a>&nbsp;
								<s:a href="%{en}"><img src="../img/english.gif" border="0" alt="en"/></s:a>&nbsp;&nbsp;
							</TD>
						</TR>
					</TABLE>
				</TD>
			</TR>
		</TABLE>
		
		<div id="tab1" style="position: absolute;top: 34px;left: 0px;visibility: hidden;">
			<TABLE BORDER=0 CELLPADDING=0 CELLSPACING=0 BGCOLOR=#FFFFFF WIDTH=150>
				<TR>
					<TD>
						<TABLE WIDTH=100% BORDER=0 CELLPADDING=0 CELLSPACING=1 BGCOLOR=#FFFFFF>
							<TR>
								<TD BGCOLOR="#000000" onMouseOver="this.style.background='#7B7B7B'" onMouseOut="this.style.background='#000000'" HEIGHT=20>
											<A href="<s:url action='midlet'/>" CLASS=ejsmenuitem><s:text name="menu.midlet.manage"/></A>
								</TD>
							</TR>
						</TABLE>
					</TD>
				</TR>
			</TABLE>
		</div>
		
		<div id="tab2" style="position: absolute;top: 34px;left: 101px;visibility: hidden;">
			<TABLE BORDER=0 CELLPADDING=0 CELLSPACING=0 BGCOLOR=#FFFFFF WIDTH=150>
				<TR>
					<TD>
						<TABLE WIDTH=100% BORDER=0 CELLPADDING=0 CELLSPACING=1 BGCOLOR=#FFFFFF>
							<TR>
								<TD BGCOLOR="#000000" onMouseOver="this.style.background='#7B7B7B'" onMouseOut="this.style.background='#000000'" HEIGHT=20>
											<A href="<s:url action='mobile'/>" CLASS=ejsmenuitem><s:text name="menu.mobile.manage"/></A>
								</TD>
							</TR>
						</TABLE>
					</TD>
				</TR>
			</TABLE>
		</div>
		
		<div id="tab3" style="position: absolute;top: 34px;left: 202px;visibility: hidden;">
			<TABLE BORDER=0 CELLPADDING=0 CELLSPACING=0 BGCOLOR=#FFFFFF WIDTH=150>
				<TR>
					<TD>
						<TABLE WIDTH=100% BORDER=0 CELLPADDING=0 CELLSPACING=1 BGCOLOR=#FFFFFF>
							<TR>
								<TD BGCOLOR="#000000" onMouseOver="this.style.background='#7B7B7B'" onMouseOut="this.style.background='#000000'" HEIGHT=20>
											<A href="<s:url action='branch'/>" CLASS=ejsmenuitem><s:text name="menu.branch.manage"/></A>
								</TD>
							</TR>
						</TABLE>
					</TD>
				</TR>
			</TABLE>
		</div>
		
		<div id="tab4" style="position: absolute;top: 34px;left: 303px;visibility: hidden;">
			<TABLE BORDER=0 CELLPADDING=0 CELLSPACING=0 BGCOLOR=#FFFFFF WIDTH=150>
				<TR>
					<TD>
						<TABLE WIDTH=100% BORDER=0 CELLPADDING=0 CELLSPACING=1 BGCOLOR=#FFFFFF>
							<TR>
								<TD BGCOLOR="#000000" onMouseOver="this.style.background='#7B7B7B'" onMouseOut="this.style.background='#000000'" HEIGHT=20>
											<A href="<s:url action='module'/>" CLASS=ejsmenuitem><s:text name="menu.module.manage"/></A>
								</TD>
							</TR>
							<TR>
								<TD BGCOLOR="#000000" onMouseOver="this.style.background='#7B7B7B'" onMouseOut="this.style.background='#000000'" HEIGHT=20>
											<A href="<s:url action='module_widget'/>" CLASS=ejsmenuitem><s:text name="menu.module.manage.widget"/></A>
								</TD>
							</TR>
							<TR>
								<TD BGCOLOR="#000000" onMouseOver="this.style.background='#7B7B7B'" onMouseOut="this.style.background='#000000'" HEIGHT=20>
											<A href="<s:url action='module_webservice'/>" CLASS=ejsmenuitem><s:text name="menu.module.manage.ws"/></A>
								</TD>
							</TR>
							<TR>
								<TD BGCOLOR="#000000" onMouseOver="this.style.background='#7B7B7B'" onMouseOut="this.style.background='#000000'" HEIGHT=20>
											<A href="<s:url action='module_library'/>" CLASS=ejsmenuitem><s:text name="menu.module.manage.library"/></A>
								</TD>
							</TR>
						</TABLE>
					</TD>
				</TR>
			</TABLE>
		</div>
		
		<div id="tab5" style="position: absolute;top: 34px;left: 404px;visibility: hidden;">
			<TABLE BORDER=0 CELLPADDING=0 CELLSPACING=0 BGCOLOR=#FFFFFF WIDTH=150>
				<TR>
					<TD>
						<TABLE WIDTH=100% BORDER=0 CELLPADDING=0 CELLSPACING=1 BGCOLOR=#FFFFFF>
							<TR>
								<TD BGCOLOR="#000000" onMouseOver="this.style.background='#7B7B7B'" onMouseOut="this.style.background='#000000'" HEIGHT=20>
											<A href="<s:url action='certif'/>" CLASS=ejsmenuitem><s:text name="menu.security.manage"/></A>
								</TD>
							</TR>
						</TABLE>
					</TD>
				</TR>
			</TABLE>
		</div>
		
		<div id="tab6" style="position: absolute;top: 34px;left: 505px;visibility: hidden;">
			<TABLE BORDER=0 CELLPADDING=0 CELLSPACING=0 BGCOLOR=#FFFFFF WIDTH=150>
				<TR>
					<TD>
						<TABLE WIDTH=100% BORDER=0 CELLPADDING=0 CELLSPACING=1 BGCOLOR=#FFFFFF>
							<TR>
								<TD BGCOLOR="#000000" onMouseOver="this.style.background='#7B7B7B'" onMouseOut="this.style.background='#000000'" HEIGHT=20>
											<A href="<s:url action='service'/>" CLASS=ejsmenuitem><s:text name="menu.service.manage"/></A>
								</TD>
							</TR>
						</TABLE>
					</TD>
				</TR>
			</TABLE>
		</div>
		
		<div id="tab7" style="position: absolute;top: 34px;left: 606px;visibility: hidden;">
			<TABLE BORDER=0 CELLPADDING=0 CELLSPACING=0 BGCOLOR=#FFFFFF WIDTH=150>
				<TR>
					<TD>
						<TABLE WIDTH=100% BORDER=0 CELLPADDING=0 CELLSPACING=1 BGCOLOR=#FFFFFF>
							<TR>
								<TD BGCOLOR="#000000" onMouseOver="this.style.background='#7B7B7B'" onMouseOut="this.style.background='#000000'" HEIGHT=20>
											<A href="<s:url action='json'/>" CLASS=ejsmenuitem><s:text name="menu.tools.json"/></A>
								</TD>
							</TR>
						</TABLE>
					</TD>
				</TR>
			</TABLE>
		</div>
		
	</DIV>
</div>
<br/><br/>
<%@ page pageEncoding="UTF-8"%>
<% new ee.netgroup.mainfuse.ServletUtil().setRequestAttributes(request); %>
<%@ page pageEncoding="UTF-8"%>
<% new ee.netgroup.mainfuse.ServletUtil().setRequestAttributes(request); %>
<!doctype html>
<html class="no-js" lang="en">
    <head>
        <meta charset="utf-8">
        <meta http-equiv="x-ua-compatible" content="ie=edge">
        <title>peakaitse</title>
        <meta name="description" content="">
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <link rel="apple-touch-icon" href="apple-touch-icon.png">
        <!-- Place favicon.ico in the root directory -->

<!--         <link rel="stylesheet" href="assets/css/app.min_1.css">
        <link rel="stylesheet" href="assets/css/app.min_2.css"> -->
        <link rel="stylesheet" href="assets/css/app.css">
        <link rel="stylesheet" href="assets/css/custom.css">

        <script src="assets/js/vendor/modernizr.js"></script>

        <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
        <!--[if lt IE 9]>
            <script src="https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js"></script>
            <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
        <![endif]-->
    <script>
        function midAuth() {
        	r=new XMLHttpRequest();
        	r.open("POST","midAuth",true);
        	r.setRequestHeader("Content-type","application/x-www-form-urlencoded");
        	r.onreadystatechange=function()
        	  {
        	  if (xmlhttp.readyState==4 && xmlhttp.status==200)
        	    {
        	    alert('Saadetud');
        	    }
        	  }
        	s = "phoneNo="+document.getElementById('phoneNumber').value;
        	r.send(s);
        }
        </script>
    </head>
    <body>
        <!--[if lt IE 10]>
            <p class="browserupgrade">You are using an <strong>outdated</strong> browser. Please <a href="http://browsehappy.com/">upgrade your browser</a> to improve your experience.</p>
        <![endif]-->

        <nav class="navbar">
            <div class="container">
                <div class="navbar-header">
                    <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
                        <span class="sr-only">Toggle navigation</span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                    </button>
                    <a class="navbar-brand" href="./">- PARAS PEAKAITSE -</a>
                </div>
                <div id="navbar" class="collapse navbar-collapse">
                    <ul class="nav navbar-nav navbar-right">
                        <!-- <li class="dropdown">
                          <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">Kullerkupu tee 8-421b, Kolga-Aabla vald, Pärnumaa <span class="caret"></span></a>
                          <ul class="dropdown-menu">
                            <li><a href="#">Action</a></li>
                            <li><a href="#">Another action</a></li>
                            <li><a href="#">Something else here</a></li>
                            <li role="separator" class="divider"></li>
                            <li><a href="#">Separated link</a></li>
                            <li role="separator" class="divider"></li>
                            <li><a href="#">One more separated link</a></li>
                          </ul>
                        </li> -->

                        <li class="dropdown">
                          <a href="#" class="dropdown-toggle header-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false"><span></span> <span class="caret"></span></a>
                          <ul class="dropdown-menu">
                            <li><a href="<%= request.getAttribute("url.startPage") %>">Välju</a></li>
                          </ul>
                        </li>
                    </ul>
                </div>
            </div>
        </nav>

        <section class="jumbotron imaged animated bounceInRight">
            <div class="container " id="js-toGo">
                <div class="row">
                    <div class="col-xs-12 col-sm-10">
                        <p>Sinu kodu peakaitse on nagu uks elektrivõrku. Peakaitse suurus määrab, kui võimsaid koduseadmeid saad üheaegselt kasutada. Külmik ja arvuti korraga vajavad väiksemat ��?ust” kui soojaveeboiler ja põrandaküte jne.
                        </p>
                        <p>
                        On oluline, et elektritarbimise suurus ja peakaitse suurus oleksid omavahel tasakaalus. Majapidamiste peakaitsete suuruste järgi planeerib võrguettevõte alajaamade ja liinide tugevused. Kui vajalikust suuremaid peakaitsmid on palju, ehitatakse vajalikust tugevam, seega ka kallim võrk. Koduomanikule võib see tähendada võrguteenuse arvel suuremat ampritasu. Vali õige suurusega peakaitse – võidad sina, võidavad su naabrid, võidab keskkond!</p>
                    </div>
                </div>
                <div class="row comeTogether">
                    <div class="col-sm-9 col-sm-offset-1 text-right animated bounceInLeft">
                        <blockquote class="blockquote-reverse">Kas Sinu kodu/ettevõtte elektrisüsteemi peakaitse on tarbimisega tasakaalus?</blockquote>
                    </div>
                    <div class="col-sm-1 animated bounceInRight">
                        <br>
                        <br>
                        <a class="btn btn-outline-inverse btn-lg" id="js-Next">Vaata järgi <i class="glyphicon glyphicon-circle-arrow-right"></i></a>
                    </div>
                </div>
            </div>
            <div class="container hide login" id="js-toCome">
                <div class="row">
                    <div class="col-sm-10 col-sm-offset-1">
                        <p>ID-kaardiga sisselogimine on vajalik, et võiksime kindlad olla Sinu ligipääsus ainult neile tarbimisandmetele, mida Sul on õigus näha.
                        </p>
                    </div>
                    <div class="col-xs-12 col-sm-8 text-right mobile-id">
                        <form action="midAuth" method="post" name="phonenoform" class="form-inline">
                            <div class="form-group">
                                <label for="phoneNumber" class="text-inverse">Telefoninumber</label>
                                <input type="text" class="form-control input-lg" id="phoneNumber"  placeholder="5055555">
                                <button onclick="javascript: midAuth()" class="btn btn-outline-inverse btn-lg" id="js-codeSent">Logi sisse Mobiil ID-ga <i class="glyphicon glyphicon-circle-arrow-right"></i></a>
                            </div>
                        </form>
                    </div>
                    <div class="col-xs-12 col-sm-4 id-card">
                        <button onclick="window.location.href='view.html'" class="btn btn-outline-inverse btn-lg" id="js-goOnMobileID">Logi sisse ID-kaardiga <i class="glyphicon glyphicon-circle-arrow-right"></i></button>
                        <p class="text-inverse success hide" id="js-comeOnMobileID">
                            Sinu telefonile on saadetud kinnituskood <strong>2375</strong>
                            <a href="./" class="btn btn-link right">Katkesta</a>
                        </p>
                        <p class="text-inverse error hide">
                            Sul pole kehtivat mobiil-ID lepingut. 
                            <a href="./" class="btn btn-link right">Katkesta</a>
                        </p>
                    </div>

                </div>
            </div>
        </section>
        <footer>
            <div class="container">
                <div class="row partner animated bounceInRight first">
                    <div class="col-sm-4">
                        <a href="./" class="estfeed"></a>
                    </div>
                    <div class="col-sm-4">
                        <a href="./" class="elektrilevi"></a>
                    </div>
                    <div class="col-sm-4">
                        <a href="./" class="nwg"></a>
                    </div>
                </div>
            </div>
        </footer>

        <script src="assets/js/vendor/jquery.min.js"></script>
        <script src="assets/js/app.js"></script>
        <script src="assets/js/custom.js"></script>
    </body>
</html>

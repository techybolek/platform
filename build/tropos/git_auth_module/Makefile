APXS=apxs2

install:
	$(APXS) -Wl,-R/opt/axis2c/lib -c -omod_appfactory_svnauth.so -I/home/damitha/projects/appfactory/axis2c/include/axis2-1.6.0 *.c -L/home/damitha/projects/appfactory/axis2c/lib -laxutil -laxis2_axiom -laxis2_engine -laxis2_parser 

clean:
	rm -rf *.lo *.la *.slo .libs

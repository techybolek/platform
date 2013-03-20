APXS=apxs2

install:
	$(APXS) -Wl,-R/opt/axis2c-1.6.0/lib -c -omod_appfactory_svnauth.so -I/opt/axis2c-1.6.0/include/axis2-1.6.0 *.c -L/opt/axis2c-1.6.0/lib -laxutil -laxis2_axiom -laxis2_engine -laxis2_parser 

clean:
	rm -rf *.lo *.la *.slo .libs

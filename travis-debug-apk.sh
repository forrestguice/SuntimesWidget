mkdir $HOME/buildResults/
cp -R app/build/outputs/apk/app-debug.apk $HOME/buildResults/

cd $HOME 
git config --global user.name "Forrest Guice"
git config --global user.email "forrestguice@gmail.com"
git clone --quiet --branch gh-pages https://forrestguice:$GITHUB_API_KEY@github.com/forrestguice/SuntimesWidget gh-pages > /dev/null

cd gh-pages/doc/debugapk
cp -Rf $HOME/buildResults/app-debug.apk suntimes-debug-b$TRAVIS_BUILD_NUMBER.apk
git add -f .

git commit -m "Travis build $TRAVIS_BUILD_NUMBER pushed [skip ci]"
git push -fq origin gh-pages > /dev/null
echo -e "Done \n"

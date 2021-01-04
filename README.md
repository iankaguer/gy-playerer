# gy-playerer
Lecteur de musique Android implementant un senseur qui permet de changer de musique en secouant


Structures
LecteurActivity est l’activité principale à laquelle sont reliés les fragments Lecteur et List.
Fonctions et Méthodes de LecteurActivity :
onBackPressed qui permet de sortir de l’application lorsque l’on appuie sur le bouton back
onDestroy permet l'arrêt du service tout en sauvegardant la dernière piste lu
onServiceConnected et onServiceDisconnected (dans serviceConnection) qui servent à lancer la connection au service
initUI qui lie les fragments à l’activité 
playAudio qui lance la lecture d’un son si aucun n’est en lecture sur le moment, il lui en fait parvenir un via le broadcast
letSeek qui suit et actualise la seekbar
updateData qui met à jour la couverture de l’album du morceaux, le titre et le nom de l’artiste

LecteurPrefModel est une classe qui permet l'enregistrement des préférences de lecture de notre lecteur mp3. Elle 
FragLecteur est un fragment du lecteur de  musique qui permet de gérer toute la partie graphique du lecteur.
FragList est un fragment de la liste des musiques qui permet de gérer la partie graphique de la liste des musiques.
CustomTouchListener permet de  récupérer l’élément de la liste sur lequel on clique.
SearchSong est une classe qui permet la mise à jour des sons de manière asynchrone.
SongListAdapter permet d’adapter la liste des sons à un recyclerview, 
SongListHolder pour représenter convenablement la liste des sons dans le recyclerview, 
SongModel est le modèle d’enregistrement des sons.
SongPlayer est le Service de Lecteur MP3 :
Ce dernier gère les différentes actions du player.
Il gère aussi les différents états du lecteur ainsi que l’état du téléphone pour le cas où le téléphone serait en appel.
Ses différentes méthodes sont :
OnStartCommand pour lancer le service
OnAudioFocusChange pour contrôler l’état du haut parleur et diminuer le volume du lecteur ou le mettre en pause en fonction de l’évènement
OnCompletion pour savoir l’action à faire à la fin de la lecture. En fonction des préférences de lecture, changer de son suivant la liste ou aléatoirement, ou lire le même son.
OnDestroy pour stopper le service tout en sauvegardant la dernière piste audio lue
OnError pour suivre les erreurs du player
OnpreparedMedia pour lancer le son une fois le service demarré
OnSensorChanged pour contrôler l’activité du senseur
Le LocalBinder permet de lier le service à l’activité
Les fonctions playMedia, stopMedia, resumeMedia et pauseMedia permettent de contrôler la lecture
initMediaPlayer initialise le lecteur
requestAudioFocus permet de demander savoir si on peut avoir l’accès au haut-parleur, s’il n’est plus utilisé par d’autres applications
initMediaSession pour initier une session de contrôle du player
updateMetaData qui met à jour les méta-informations de la piste lue
skipToNext et skipToPrev permettent de changer la musique en fonction des preferences
getCurrentAudioPosition permet de récupérer la position du son lu actuellement dans la liste des sons
removeNotification pour supprimer les notifications à la fermeture du service
Il y’a également les broadcast removingHeadphone qui permet de mettre en pause la musique quand les écouteurs sont débranchés et  playNewAudio pour lancer une nouvelle piste quand le mediaplayer est déjà entrain de jouer un son

handleIncomingActions permet de contrôler la musique depuis la barre de notifications grâce au pendingIntent playBackAction
buildNotification permet de mettre en place les notification grâce aux informations sur la lecture actuelle

SplashActivity est l’activité d'introduction de notre player, il y a une animation du logo pendant qu’en arrière plan on contrôle les autorisations et on met à jours la liste des sons





	


Permissions
L’application a eu besoin des des permissions suivantes :
accelerometer : pour pouvoir permettre la rotation de l’application lorsque le téléphone tourne sur lui-même.  
Media_CONTENT_CONTROL pour contrôler la musique lu à travers la barre de notification
READ_EXTERNAL_STORAGE et WRITE_EXTERNAL_STORAGE permettent d’avoir accès en lecture et en écriture au stockage externe, pour faire des recherches de musiques.
READ_PHONE_STATE permet de savoir si le téléphone est en cours d’appel, pour mettre l’application en pause.


Librairies
En plus du recyclerview, nous avons au cours de ce projet utilisé les librairies externes à android. il s’agit des librairies
Glaussian Blur pour flouter une image afin de le mettre en background
Realm : comme base de données
Palette API : pour récupérer les couleurs dominantes/non dominantes sur une image
bubblenavigation pour l'affichage sympa des boutons de navigation des fragments 
Bodymovin pour l'animation du logo préalablement fait avec Adobe AfterEffects puis exportés en json et ajoutés au projet

Capteur : 
Nous avons utilisé le capteur TYPE_ACCELEROMETER pour contrôler le changement de musique


Thread
Il ya un thread qui lors de la lecture met à jour les méta-informations de la chanson et un autre qui met à jour la progression de la lecture


Layout
Les layouts utilisés dans dons projet :
ConstraintLayout
LinearLayout
RelativeLayout



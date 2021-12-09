<template>
  <div>
    <v-card class="betop">
      <BaseGoBack class="ml-6" style="float:left"></BaseGoBack>
      <div class="ma-auto d-inline-flex">
        <v-icon 
          class="ml-5 text-h2"
          color="indigo darken-4"
        >mdi-clipboard-account
        </v-icon>
        <div class="my-font my-5">
          Door
        </div>
      </div>
    </v-card>
    <div class="whole">
      <v-row>
        <v-col cols="3">
          <v-card
            class="mx-auto left"
            width="256"
            height="600"
            tile
          >
            <v-navigation-drawer permanent>
              <v-system-bar></v-system-bar>
              <v-list>
                <v-list-item>
                  <v-list-item-avatar>
                    <v-img src="https://cdn.vuetifyjs.com/images/john.png"></v-img>
                  </v-list-item-avatar>
                </v-list-item>

                <v-list-item link>
                  <v-list-item-content>
                    <v-list-item-title class="title">John Leider</v-list-item-title>
                    <v-list-item-subtitle>john@vuetifyjs.com</v-list-item-subtitle>
                  </v-list-item-content>

                  <v-list-item-action>
                    <v-icon>mdi-menu-down</v-icon>
                  </v-list-item-action>
                </v-list-item>
              </v-list>
              <v-divider></v-divider>
              <v-list
                nav
                dense
              >
                <v-list-item-group v-model="item" color="primary">
                  <v-list-item @click="mDoor()">
                    <v-list-item-icon>
                      <v-icon v-text="item.icon"></v-icon>
                    </v-list-item-icon>

                    <v-list-item-content>门户管理</v-list-item-content>
                  </v-list-item>
                  <v-list-item @click="mPaper()">
                    <v-list-item-icon>
                      <v-icon v-text="item.icon"></v-icon>
                    </v-list-item-icon>

                    <v-list-item-content>文献管理</v-list-item-content>
                  </v-list-item>
                </v-list-item-group>
              </v-list>
            </v-navigation-drawer>
          </v-card>
        </v-col>
        <v-col v-if="manage==0" offset-md="1">
          <div style="padding-top:10px;">
            <v-row  class="mb-6">
              <v-btn>认领新的门户</v-btn>
            </v-row>
            <v-row v-for="item in doors" :key=item.id class="mb-6">
            
              <v-card
                width="500"
              >
                <v-card-title>{{item.name}}</v-card-title>
              </v-card>
            </v-row>
          </div>
          
        </v-col>
        <v-col v-if="manage==1" offset-md="1">
          <div style="padding-top:10px;">
            <v-row v-for="item in papers" :key=item.id class="mb-6">
            
              <v-card
                width="500"
              >
                <v-card-title>{{item.name}}</v-card-title>
              </v-card>
            </v-row>
          </div>
          
        </v-col>
      </v-row>
    </div>
  </div>
  
  
</template>

<script>
  export default {
    data: () => ({
      manage:0,
      papers:[
        {id:1,name:"文献1"},
        {id:2,name:"文献2"},
      ],
      doors:[
        {id:1,name:"门户1"},
        {id:2,name:"门户2"},
      ],
      item: 0,
      items: [
        { text: 'My Files', icon: 'mdi-folder' },
        { text: 'Shared with me', icon: 'mdi-account-multiple' },
        { text: 'Starred', icon: 'mdi-star' },
        { text: 'Recent', icon: 'mdi-history' },
        { text: 'Offline', icon: 'mdi-check-circle' },
        { text: 'Uploads', icon: 'mdi-upload' },
        { text: 'Backups', icon: 'mdi-cloud-upload' },
      ],
    }),
    methods:{
      mDoor(){
        this.manage=0
      },
      mPaper(){
        this.manage=1
      }
    }
  }
</script>
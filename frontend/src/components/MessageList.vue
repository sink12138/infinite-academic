<template>
  <v-card>
    <v-list
      class="text-left pa-0"
    >
      <v-list-group
        v-for="item in types"
        :key="item.type"
        v-show="item.items.length != 0"
        prepend-icon="mdi-arrow-down-drop-circle"
        class="grey lighten-3"
        color="grey darken-2"
        sub-group
      >
        <template v-slot:activator>
          <v-icon>{{item.icon}}</v-icon>
          <v-list-item-content>
            <v-list-item-title v-text="item.type"></v-list-item-title>
          </v-list-item-content>
        </template>

        <v-list-item
          v-for="child in item.items"
          :key="child.title"
          class="pl-8 item-color"
        >
        
          <v-chip
            v-if="child.status == '审核通过'"
            dark
          >
            <v-icon color="green lighten-3">
              mdi-checkbox-marked
            </v-icon>
            <span>已通过</span>
          </v-chip>
          <v-chip
            v-if="child.status == '审核不通过'"
            dark
          >
            <v-icon color="red lighten-3">
              mdi-close-box
            </v-icon>
            <span>未通过</span>
          </v-chip>
          <v-chip
            v-if="child.status == '审核中'"
            dark
          >
            <v-icon color="blue lighten-4">
              mdi-timer-sand-full
            </v-icon>
            <span>审核中</span>
          </v-chip>

          <v-list-item-content class="ml-2">
            {{child.type}} : {{child.title}}
          </v-list-item-content>

          <v-spacer></v-spacer>

          <v-icon>
            mdi-clock
          </v-icon>
          <v-list-item-content>
            {{child.time}}
          </v-list-item-content>
          
          <v-spacer></v-spacer>

          <message-dialog :message="child"></message-dialog>
          
        </v-list-item>
        <v-divider></v-divider>
      </v-list-group>
    </v-list>
  </v-card>
</template>
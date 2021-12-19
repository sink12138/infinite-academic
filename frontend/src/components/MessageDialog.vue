<template>
  <v-dialog
    v-model="dialog"
    width="500"
  >
    <template v-slot:activator="{ on, attrs }">
      <v-btn
        v-bind="attrs"
        v-on="on"
        small
        @click="getDetails()"
      >
        <v-icon>mdi-information-outline</v-icon>
        详细
      </v-btn>
    </template>

    <v-card>
      <v-card-title class="text-h5 grey lighten-2">
        Privacy Policy
      </v-card-title>

      <v-card-text>
        {{message.title}}
      </v-card-text>

      <v-divider></v-divider>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn
          color="primary"
          text
          @click="dialog = false"
        >
          I accept
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
export default {
  data() {
    return {
      dialog: false,
    }
  },
  props: {
    message: {
      type: Object,
      default:() => {}
    }
  },
  methods: {
    getDetails() {
      this.$axios({
        method: "get",
        url: "/api/account/application/details/"+this.message.id,
      }).then(res => {
        console.log(res.data)
      }).catch(error => {
        console.log(error)
      })
    }
  }
}
</script>

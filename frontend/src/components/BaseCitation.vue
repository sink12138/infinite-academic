<template>
  <div>
    <v-menu
      :close-on-content-click="false"
      :nudge-width="200"
      offset-y
    >
      <template v-slot:activator="{ on, attrs }">
        <v-btn
          v-bind="attrs"
          v-on="on"
        >
          Citation
          <v-icon>mdi-comma</v-icon>
        </v-btn>
      </template>

      <v-card
        class="mt-100"
      >
        <v-card-title>
          Citation List
        </v-card-title>
        <v-card-text>
          ...TEXT...
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn @click="citationCopy">Copy</v-btn>
        </v-card-actions>
      </v-card>
    </v-menu>
  </div>
</template>

<script>
export default {
  data:() =>  ({
    expand: false,
    citationList: 'hello'
  }),
  methods: {
    citationCopy() {
      this.$copyText(this.citationList)
      .then(
        function(e) {
          console.log(e);
          this.$notify({
            title: 'hello',
            message: 'test message',
            type: 'success'
          });
        }
      )
      this.$notify({
        title: 'hello',
        message: 'test message',
        type: 'success'
      });
    },
    onClickOutside() {
      this.expand = false;
    }
  }
}
</script>

<style>

</style>